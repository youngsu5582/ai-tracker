import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

function PromptList() {
    const { category } = useParams();
    const [prompts, setPrompts] = useState([]);

    useEffect(() => {
        axios.get(`${process.env.REACT_APP_API_BASE_URL}/prompts?category=${category}`)
            .then(response => {
                setPrompts(response.data);
            })
            .catch(error => {
                console.error(`Error fetching prompts for category ${category}:`, error);
            });
    }, [category]);

    return (
        <div className="prompt-list-page">
            <h2>Prompts for: {category}</h2>
            <div className="timeline">
                {prompts.map(prompt => (
                    <div key={prompt.id} className="timeline-item">
                        <div className="timeline-item-time">{new Date(prompt.timestamp).toLocaleString()}</div>
                        <div className="timeline-item-content">
                            <p className="prompt-text">{prompt.prompt}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default PromptList;
