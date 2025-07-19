import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './History.css';

// Helper function to format date to YYYY-MM-DD using local time
const formatDate = (date) => {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
};

function History() {
    const [history, setHistory] = useState({});
    const [startDate, setStartDate] = useState(formatDate(new Date())); // Default to today
    const [endDate, setEndDate] = useState(formatDate(new Date())); // Default to today
    const [expandedPrompts, setExpandedPrompts] = useState({}); // To manage "read more" state
    const [showMeaningless, setShowMeaningless] = useState(true); // New state for filtering meaningless prompts

    // Function to fetch prompts based on date range
    const fetchPrompts = (start, end) => {
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts/history?startDate=${start}&endDate=${end}`)
            .then(response => {
                // Group prompts by date for timeline display
                const groupedByDate = response.data.reduce((acc, prompt) => {
                    // Convert timestamp to local date string for grouping
                    const utcTimestamp = prompt.timestamp.endsWith('Z') ? prompt.timestamp : prompt.timestamp + 'Z';
                    const dateObj = new Date(utcTimestamp);
                    const localDate = dateObj.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long', timeZone: 'Asia/Seoul' });
                    if (!acc[localDate]) {
                        acc[localDate] = [];
                    }
                    acc[localDate].push(prompt);
                    return acc;
                }, {});
                setHistory(groupedByDate);
            })
            .catch(error => {
                
                setHistory({}); // Clear history on error
            });
    };

    useEffect(() => {
        fetchPrompts(startDate, endDate);
    }, [startDate, endDate]); // Re-fetch when date range changes

    // Date preset handlers
    const setToday = () => {
        const today = new Date();
        setStartDate(formatDate(today));
        setEndDate(formatDate(today));
    };

    const setLast7Days = () => {
        const today = new Date();
        const sevenDaysAgo = new Date(today);
        sevenDaysAgo.setDate(today.getDate() - 6); // 7 days including today
        setStartDate(formatDate(sevenDaysAgo));
        setEndDate(formatDate(today));
    };

    const setLast30Days = () => {
        const today = new Date();
        const thirtyDaysAgo = new Date(today);
        thirtyDaysAgo.setDate(today.getDate() - 29); // 30 days including today
        setStartDate(formatDate(thirtyDaysAgo));
        setEndDate(formatDate(today));
    };

    // Toggle "read more" for prompt text
    const toggleExpand = (id) => {
        setExpandedPrompts(prev => ({
            ...prev,
            [id]: !prev[id]
        }));
    };

    // Function to truncate text
    const truncateText = (text, limit = 200) => {
        if (text.length <= limit) {
            return { truncated: false, text: text };
        }
        return { truncated: true, text: text.substring(0, limit) + '...' };
    };


    return (
        <div className="history-page">
            <h1>AI Prompt History</h1> {/* Added a title */}
            <div className="history-controls">
                <input
                    type="date"
                    value={startDate}
                    onChange={e => setStartDate(e.target.value)}
                />
                <span>~</span>
                <input
                    type="date"
                    value={endDate}
                    onChange={e => setEndDate(e.target.value)}
                />
                <button onClick={setToday}>오늘</button>
                <button onClick={setLast7Days}>최근 7일</button>
                <button onClick={setLast30Days}>최근 30일</button>
                <label className="filter-checkbox">
                    <input
                        type="checkbox"
                        checked={showMeaningless}
                        onChange={() => setShowMeaningless(!showMeaningless)}
                    />
                    의미 없는 프롬프트 보기
                </label>
            </div>

            <div className="timeline">
                {Object.entries(history).length === 0 ? (
                    <p className="no-data-message">선택된 기간에 프롬프트 기록이 없습니다.</p>
                ) : (
                    Object.entries(history).sort(([dateA], [dateB]) => new Date(dateB) - new Date(dateA)).map(([day, prompts]) => (
                        <div key={day} className="timeline-day">
                            <div className="timeline-day-header">{day}</div>
                            {prompts.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp)).map(prompt => {
                                if (!showMeaningless && prompt.isMeaningless) {
                                    return null; // Don't render if we're hiding meaningless and it is meaningless
                                }
                                const { truncated, text } = truncateText(prompt.prompt, 200);
                                const isExpanded = expandedPrompts[prompt.id];
                                return (
                                    <div key={prompt.id} className="timeline-item">
                                        <div className="timeline-item-time">
                                            {new Date(prompt.timestamp + 'Z').toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Seoul' })}
                                        </div>
                                        <div className="timeline-item-content">
                                            <div className="prompt-meta-tags">
                                                {prompt.source && <span className="tag source-tag">{prompt.source}</span>}
                                                {prompt.model && <span className="tag model-tag">{prompt.model}</span>}
                                                {prompt.language && <span className="tag language-tag">{prompt.language}</span>}
                                                {prompt.category && <span className="tag category-tag">{prompt.category}</span>}
                                            </div>
                                            <p className="prompt-text">
                                                <span className={prompt.isMeaningless ? "meaningless-prompt-text" : ""}>
                                                    {isExpanded ? prompt.prompt : text}
                                                </span>
                                            </p>
                                            {truncated && (
                                                <button onClick={() => toggleExpand(prompt.id)} className="read-more-btn">
                                                    {isExpanded ? '접기' : '더보기'}
                                                </button>
                                            )}
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
                                    </div>
                                );
                            })}
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default History;