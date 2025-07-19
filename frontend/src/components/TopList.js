import React from 'react';

function TopList({ title, data, icon, itemIcons = {} }) {
    return (
        <div className="card">
            <h3>{icon} {title}</h3>
            <ul>
                {Object.entries(data).map(([key, value]) => (
                    <li key={key}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            {itemIcons[key] && <span style={{ fontSize: '1.2em' }}>{itemIcons[key]}</span>}
                            {key}
                        </span>
                        <span>{value}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default TopList;
