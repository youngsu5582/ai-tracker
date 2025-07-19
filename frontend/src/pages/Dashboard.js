import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Summary from '../components/Summary';
import { FaChartBar } from 'react-icons/fa'; // Import an icon for Summary

function Dashboard() {
    const [stats, setStats] = useState(null);
    const [period, setPeriod] = useState('daily');

    useEffect(() => {
        const fetchStats = () => {
            axios.get(`${process.env.REACT_APP_API_BASE_URL}/data/statistics?period=${period}`)
                .then(response => {
                    setStats(response.data);
                })
                .catch(error => {
                    console.error(`Error fetching ${period} statistics:`, error);
                });
        };

        fetchStats();
        const interval = setInterval(fetchStats, 5000); // Refresh every 5 seconds

        return () => clearInterval(interval);
    }, [period]);

    return (
        <div>
            <div className="period-selector">
                <button onClick={() => setPeriod('daily')}>Daily</button>
                <button onClick={() => setPeriod('weekly')}>Weekly</button>
                <button onClick={() => setPeriod('monthly')}>Monthly</button>
            </div>
            {stats ? <Summary stats={stats} period={period} /> : <p>Loading statistics...</p>}
        </div>
    );
}

export default Dashboard;
