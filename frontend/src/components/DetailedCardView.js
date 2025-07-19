import React from 'react';
import { Bar, Doughnut } from 'react-chartjs-2';
import TopList from './TopList';
import TimelineChart from './TimelineChart';
import CategoryTree from './CategoryTree';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
} from 'chart.js';
import { FaChartBar, FaChartPie, FaList, FaClock, FaFolder, FaGlobe, FaCommentDots, FaGem } from 'react-icons/fa';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

const isDataValid = (data) => data && Object.keys(data).length > 0;

function DetailedCardView({ type, data, title, chartOptions, siteIcons, period }) {
    switch (type) {
        case 'totalRequests':
            return (
                <div>
                    <h2><FaChartBar /> {title}</h2>
                    <p style={{ fontSize: '3em', textAlign: 'center' }}>{data || 0}</p>
                </div>
            );
        case 'topList':
            return <TopList title={title} data={data} icon={title === "Top Sites" ? <FaGlobe /> : <FaList />} itemIcons={siteIcons} />;
        case 'timelineChart':
            return <TimelineChart data={data} icon={<FaClock />} period={period} />;
        case 'categoryTree':
            return <CategoryTree data={data} icon={<FaFolder />} />;
        case 'barChart':
            return (
                <div>
                    <h2><FaChartBar /> {title}</h2>
                    <Bar data={data} options={chartOptions} />
                </div>
            );
        case 'doughnutChart':
            return (
                <div>
                    <h2><FaChartPie /> {title}</h2>
                    <Doughnut data={data} options={chartOptions} />
                </div>
            );
        default:
            return <p>No detailed view available for this type.</p>;
    }
}

export default DetailedCardView;
