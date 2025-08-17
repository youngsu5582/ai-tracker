import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './History.css';

const formatDate = (date) => {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
};

function History() {
    const [conversations, setConversations] = useState({});
    const [startDate, setStartDate] = useState(formatDate(new Date()));
    const [endDate, setEndDate] = useState(formatDate(new Date()));
    const [expandedConversations, setExpandedConversations] = useState({});
    const [expandedMessages, setExpandedMessages] = useState({});
    const [showMeaningless, setShowMeaningless] = useState(true);

    const fetchPrompts = (start, end) => {
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts/history?startDate=${start}&endDate=${end}`)
            .then(response => {
                const groupedByConversation = response.data.reduce((acc, prompt) => {
                    const conversationId = prompt.conversationId || `no-conv-${prompt.id}`;
                    if (!acc[conversationId]) {
                        acc[conversationId] = [];
                    }
                    acc[conversationId].push(prompt);
                    return acc;
                }, {});
                setConversations(groupedByConversation);
            })
            .catch(error => {
                setConversations({});
            });
    };

    useEffect(() => {
        fetchPrompts(startDate, endDate);
    }, [startDate, endDate]);

    const toggleConversation = (conversationId) => {
        setExpandedConversations(prev => ({
            ...prev,
            [conversationId]: !prev[conversationId]
        }));
    };

    const toggleMessage = (messageId) => {
        setExpandedMessages(prev => ({
            ...prev,
            [messageId]: !prev[messageId]
        }));
    };

    const setToday = () => {
        const today = new Date();
        setStartDate(formatDate(today));
        setEndDate(formatDate(today));
    };

    const setLast7Days = () => {
        const today = new Date();
        const sevenDaysAgo = new Date(today);
        sevenDaysAgo.setDate(today.getDate() - 6);
        setStartDate(formatDate(sevenDaysAgo));
        setEndDate(formatDate(today));
    };

    const setLast30Days = () => {
        const today = new Date();
        const thirtyDaysAgo = new Date(today);
        thirtyDaysAgo.setDate(today.getDate() - 29);
        setStartDate(formatDate(thirtyDaysAgo));
        setEndDate(formatDate(today));
    };

    return (
        <div className="history-page">
            <h1>AI Prompt History</h1>
            <div className="history-controls">
                <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} />
                <span>~</span>
                <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} />
                <button onClick={setToday}>오늘</button>
                <button onClick={setLast7Days}>최근 7일</button>
                <button onClick={setLast30Days}>최근 30일</button>
                <label className="filter-checkbox">
                    <input type="checkbox" checked={showMeaningless} onChange={() => setShowMeaningless(!showMeaningless)} />
                    의미 없는 프롬프트 보기
                </label>
            </div>

            <div className="timeline">
                {Object.entries(conversations).length === 0 ? (
                    <p className="no-data-message">선택된 기간에 프롬프트 기록이 없습니다.</p>
                ) : (
                    Object.entries(conversations).sort(([_, promptsA], [__, promptsB]) => new Date(promptsB[0].timestamp) - new Date(promptsA[0].timestamp)).map(([conversationId, prompts]) => {
                        const firstPrompt = prompts[0];
                        if (!showMeaningless && firstPrompt.isMeaningless) {
                            return null;
                        }
                        return (
                            <div key={conversationId} className="timeline-conversation-group">
                                <div className="timeline-item timeline-conversation-header" onClick={() => toggleConversation(conversationId)}>
                                    <div className="timeline-item-time">
                                        {new Date(firstPrompt.timestamp).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Seoul' })}
                                    </div>
                                    <div className="timeline-item-content">
                                        <p className="prompt-text">{firstPrompt.prompt}</p>
                                        <div className="prompt-meta-tags">
                                            <span className="tag conversation-tag">{prompts.length} messages</span>
                                        </div>
                                    </div>
                                </div>
                                {expandedConversations[conversationId] && (
                                    <div className="conversation-details">
                                        {prompts.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp)).map(prompt => (
                                            <div key={prompt.id} className="timeline-item-container">
                                                <div className="timeline-item-content" onClick={() => toggleMessage(prompt.id)}>
                                                    <div className="prompt-meta-tags">
                                                        {prompt.source && <span className="tag source-tag">{prompt.source}</span>}
                                                        {prompt.model && <span className="tag model-tag">{prompt.model}</span>}
                                                        {prompt.language && <span className="tag language-tag">{prompt.language}</span>}
                                                        {prompt.category && <span className="tag category-tag">{prompt.category}</span>}
                                                        {prompt.mainKeyword && <span className="tag keyword-display-tag">{prompt.mainKeyword}</span>}
                                                    </div>
                                                    <p className="prompt-text"><b>Prompt:</b> {prompt.prompt}</p>
                                                </div>
                                                {expandedMessages[prompt.id] && (
                                                    <div className="timeline-item-details">
                                                        <div className="response-text">
                                                            <b>Response:</b>
                                                            <ReactMarkdown remarkPlugins={[remarkGfm]}>{prompt.response}</ReactMarkdown>
                                                        </div>
                                                        {prompt.score !== undefined && prompt.score !== null && (
                                                            <div className="prompt-evaluation-score">
                                                                <strong>평가 점수:</strong> {prompt.score} / 10
                                                            </div>
                                                        )}
                                                        {prompt.evaluationReasons && prompt.evaluationReasons.length > 0 && (
                                                            <div className="prompt-evaluation-reasons">
                                                                <strong>평가 근거:</strong>
                                                                <ul>
                                                                    {prompt.evaluationReasons.map((reason, index) => (
                                                                        <li key={index}>{reason}</li>
                                                                    ))}
                                                                </ul>
                                                            </div>
                                                        )}
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
}

export default History;