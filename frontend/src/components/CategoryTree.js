import React from 'react';

function CategoryTree({ data, icon }) {
    return (
        <div className="card">
            <h3>{icon} Category Tree</h3>
            <ul>
                {Object.entries(data).map(([key, value]) => (
                    <li key={key}>
                        <span>{key}</span>
                        <span>{value}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default CategoryTree;
