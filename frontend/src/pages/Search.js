import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './Search.css';

function Search() {
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState({});
    const [allKeywords, setAllKeywords] = useState([]);
    const [allTags, setAllTags] = useState([]);
    const [expandedConversations, setExpandedConversations] = useState({});
    const [expandedMessages, setExpandedMessages] = useState({});
    const [showFullConversationModal, setShowFullConversationModal] = useState(false);
    const [fullConversationData, setFullConversationData] = useState([]);

    useEffect(() => {
        // Fetch all main keywords
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts/keywords`)
            .then(response => {
                setAllKeywords(response.data);
            })
            .catch(error => {
                console.error("Error fetching main keywords:", error);
            });

        // Fetch all tags
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts/tags`)
            .then(response => {
                setAllTags(response.data);
            })
            .catch(error => {
                console.error("Error fetching tags:", error);
            });
    }, []);

    const handleSearch = (keywordToSearch = searchKeyword) => {
        if (keywordToSearch.trim() === '') {
            setSearchResults({});
            return;
        }
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts/search?keyword=${encodeURIComponent(keywordToSearch)}`)
            .then(response => {
                const groupedByConversation = response.data.reduce((acc, prompt) => {
                    const conversationId = prompt.conversationId || `no-conv-${prompt.id}`;
                    if (!acc[conversationId]) {
                        acc[conversationId] = [];
                    }
                    acc[conversationId].push(prompt);
                    return acc;
                }, {});
                setSearchResults(groupedByConversation);
            })
            .catch(error => {
                console.error("Error fetching search results:", error);
                setSearchResults({});
            });
    };

    const handleKeywordClick = (keyword) => {
        setSearchKeyword(keyword);
        handleSearch(keyword);
    };

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

    const fetchFullConversation = (conversationId) => {
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts/conversation/${conversationId}`)
            .then(response => {
                // Sort messages by timestamp for chronological display
                const sortedPrompts = response.data.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
                setFullConversationData(sortedPrompts);
                setShowFullConversationModal(true);
            })
            .catch(error => {
                console.error("Error fetching full conversation:", error);
                setFullConversationData([]);
            });
    };

    const closeFullConversationModal = () => {
        setShowFullConversationModal(false);
        setFullConversationData([]);
    };

    return (
        <div className="search-page">
            <h1>Search AI Prompts</h1>

            <div className="search-input-section">
                <input
                    type="text"
                    placeholder="Enter keyword to search..."
                    value={searchKeyword}
                    onChange={e => setSearchKeyword(e.target.value)}
                    onKeyPress={e => { if (e.key === 'Enter') handleSearch(); }}
                />
                <button onClick={() => handleSearch()}>Search</button>
            </div>

            <div className="keywords-section">
                <h2>Available Keywords</h2>
                <div className="keyword-list">
                    {allKeywords.length === 0 ? (
                        <p>No keywords available yet. Start generating some prompts!</p>
                    ) : (
                        allKeywords.map(keyword => (
                            <span key={keyword} className="keyword-tag" onClick={() => handleKeywordClick(keyword)}>
                                {keyword}
                            </span>
                        ))
                    )}
                </div>
            </div>

            <div className="tags-section">
                <h2>Available Tags</h2>
                <div className="tag-list">
                    {allTags.length === 0 ? (
                        <p>No tags available yet.</p>
                    ) : (
                        allTags.map(tag => (
                            <span key={tag} className="tag-item" onClick={() => handleKeywordClick(tag)}>
                                {tag}
                            </span>
                        ))
                    )}
                </div>
            </div>

            <div className="search-results-section">
                <h2>Search Results ({Object.values(searchResults).flat().length} results)</h2>
                {Object.entries(searchResults).length === 0 ? (
                    <p className="no-results-message">No results found. Try a different keyword or generate more prompts.</p>
                ) : (
                    Object.entries(searchResults).sort(([_, promptsA], [__, promptsB]) => new Date(promptsB[0].timestamp) - new Date(promptsA[0].timestamp)).map(([conversationId, prompts]) => {
                        const firstPrompt = prompts[0];
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
                                            {firstPrompt.conversationId && firstPrompt.conversationId !== `no-conv-${firstPrompt.id}` && (
                                                <button onClick={(e) => {
                                                    e.stopPropagation(); // Prevent toggling conversation group
                                                    fetchFullConversation(firstPrompt.conversationId);
                                                }} className="view-full-conversation-btn">
                                                    View Full Conversation
                                                </button>
                                            )}
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

            {showFullConversationModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <button className="modal-close-btn" onClick={closeFullConversationModal}>&times;</button>
                        <h2>Full Conversation</h2>
                        <div className="full-conversation-display">
                            {fullConversationData.map(prompt => (
                                <div key={prompt.id} className="full-conversation-item">
                                    <div className="full-conversation-meta">
                                        <span className="tag source-tag">{prompt.source}</span>
                                        <span className="tag model-tag">{prompt.model}</span>
                                        <span className="tag language-tag">{prompt.language}</span>
                                        <span className="tag category-tag">{prompt.category}</span>
                                        <span className="full-conversation-time">
                                            {new Date(prompt.timestamp).toLocaleString('ko-KR', { dateStyle: 'short', timeStyle: 'short', timeZone: 'Asia/Seoul' })}
                                        </span>
                                    </div>
                                    <p className="full-conversation-prompt"><b>Prompt:</b> {prompt.prompt}</p>
                                    <div className="full-conversation-response">
                                        <b>Response:</b>
                                        <ReactMarkdown remarkPlugins={[remarkGfm]}>{prompt.response}</ReactMarkdown>
                                    </div>
                                    {prompt.score !== undefined && prompt.score !== null && (
                                        <div className="full-conversation-evaluation-score">
                                            <strong>평가 점수:</strong> {prompt.score} / 10
                                        </div>
                                    )}
                                    {prompt.evaluationReasons && prompt.evaluationReasons.length > 0 && (
                                        <div className="full-conversation-evaluation-reasons">
                                            <strong>평가 근거:</strong>
                                            <ul>
                                                {prompt.evaluationReasons.map((reason, index) => (
                                                    <li key={index}>{reason}</li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Search;