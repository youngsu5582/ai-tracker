import React from 'react';
import { Bar } from 'react-chartjs-2';

function TimelineChart({ data, icon, period }) {
    const getTimelineTitle = (currentPeriod) => {
        switch (currentPeriod) {
            case 'daily': return 'Timeline (by Hour)';
            case 'weekly': return 'Timeline (by Day)';
            case 'monthly': return 'Timeline (by Day)';
            default: return 'Timeline';
        }
    };

    const chartData = {
        labels: Object.keys(data),
        datasets: [{
            label: `Requests by ${period === 'daily' ? 'Hour' : 'Day'}`,
            data: Object.values(data),
            backgroundColor: 'rgba(255, 159, 64, 0.8)',
            categoryPercentage: 1.0,
            barPercentage: 1.0,
        }]
    };

    const options = {
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            x: {
                ticks: {
                    color: '#333' // Make x-axis labels visible
                },
                grid: {
                    color: '#eee' // Lighter grid lines
                }
            },
            y: {
                ticks: {
                    color: '#333' // Make y-axis labels visible
                },
                grid: {
                    color: '#eee' // Lighter grid lines
                }
            }
        }
    };

    return (
        <div className="card">
            <h3>{icon} {getTimelineTitle(period)}</h3>
            <Bar data={chartData} options={options} />
        </div>
    );
}

export default TimelineChart;
