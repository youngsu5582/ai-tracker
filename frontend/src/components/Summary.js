import React, { useState } from 'react';
import { Bar, Doughnut, getElementAtEvent } from 'react-chartjs-2';
import { useNavigate } from 'react-router-dom';
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
import { FaChartBar, FaChartPie, FaList, FaClock, FaFolder, FaGlobe, FaCommentDots, FaGem } from 'react-icons/fa'; // Import icons
import Modal from './Modal';
import DetailedCardView from './DetailedCardView';
import TopList from './TopList';
import TimelineChart from './TimelineChart';
import CategoryTree from './CategoryTree';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

// Helper function to check if data is valid for charting
const isDataValid = (data) => data && Object.keys(data).length > 0;

function Summary({ stats, period }) {
    const navigate = useNavigate();
    const chartRef = React.useRef();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalContent, setModalContent] = useState(null);

    const handleChartClick = (event, elements) => {
        if (!elements || elements.length === 0) return;
        const element = elements[0];
        const category = chartRef.current.data.labels[element.index];
        navigate(`/prompts/${category}`);
    };

    const handleCardClick = (type, data, title, chartOptions = null, period = null) => {
        setModalContent({ type, data, title, chartOptions, period });
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setModalContent(null);
    };

    const chartOptions = {
        plugins: {
            legend: {
                labels: {
                    color: '#333' // Dark text for legend
                }
            }
        },
        scales: {
            x: {
                ticks: {
                    color: '#333' // Dark text for x-axis labels
                },
                grid: {
                    color: '#eee' // Lighter grid lines
                }
            },
            y: {
                ticks: {
                    color: '#333' // Dark text for y-axis labels
                },
                grid: {
                    color: '#eee' // Lighter grid lines
                }
            }
        }
    };

    const requestsBySiteData = isDataValid(stats.requestsBySite) ? {
        labels: Object.keys(stats.requestsBySite),
        datasets: [{
            label: 'Requests by Site',
            data: Object.values(stats.requestsBySite),
            backgroundColor: 'rgba(75, 192, 192, 0.8)',
        }]
    } : null;

    const requestsByModelData = isDataValid(stats.requestsByModel) ? {
        labels: Object.keys(stats.requestsByModel),
        datasets: [{
            label: 'Requests by Model',
            data: Object.values(stats.requestsByModel),
            backgroundColor: 'rgba(153, 102, 255, 0.8)',
        }]
    } : null;

    const requestsByCategoryData = isDataValid(stats.requestsByCategory) ? {
        labels: Object.keys(stats.requestsByCategory),
        datasets: [{
            label: 'Requests by Category',
            data: Object.values(stats.requestsByCategory),
            backgroundColor: [
                'rgba(255, 99, 132, 0.8)',
                'rgba(54, 162, 235, 0.8)',
                'rgba(255, 206, 86, 0.8)',
                'rgba(75, 192, 192, 0.8)',
                'rgba(153, 102, 255, 0.8)',
                'rgba(255, 159, 64, 0.8)'
            ],
            borderColor: '#fff' // White border for doughnut chart
        }]
    } : null;

    const siteIcons = {
        'chatgpt.com': <FaCommentDots style={{ color: '#10a37f' }} />,
        'gemini.google.com': <FaGem style={{ color: '#4285F4' }} />,
        // Add more site icons here as needed
    };

    return (
        <div className="dashboard">
            <div className="card" onClick={() => handleCardClick('totalRequests', stats.totalRequests, 'Total Requests')}>
                <h3><FaChartBar /> Total Requests</h3>
                <p style={{ fontSize: '2em', margin: 0 }}>{stats.totalRequests || 0}</p>
            </div>
            {isDataValid(stats.topSites) && (
                <div className="card" onClick={() => handleCardClick('topList', stats.topSites, 'Top Sites', siteIcons)}>
                    <TopList title="Top Sites" data={stats.topSites} icon={<FaGlobe />} itemIcons={siteIcons} />
                </div>
            )}
            {isDataValid(stats.topModels) && (
                <div className="card" onClick={() => handleCardClick('topList', stats.topModels, 'Top Models')}>
                    <TopList title="Top Models" data={stats.topModels} icon={<FaList />} />
                </div>
            )}
            {isDataValid(stats.requestsByHour) && (
                <div className="card" onClick={() => handleCardClick('timelineChart', stats.requestsByHour, 'Timeline', null, period)}>
                    <TimelineChart data={stats.requestsByHour} icon={<FaClock />} period={period} />
                </div>
            )}
            {isDataValid(stats.categoryTree) && (
                <div className="card" onClick={() => handleCardClick('categoryTree', stats.categoryTree, 'Category Tree')}>
                    <CategoryTree data={stats.categoryTree} icon={<FaFolder />} />
                </div>
            )}
            {requestsBySiteData && (
                <div className="card" onClick={() => handleCardClick('barChart', requestsBySiteData, 'Requests by Site', chartOptions)}>
                    <h3><FaChartBar /> Requests by Site</h3>
                    <Bar data={requestsBySiteData} options={chartOptions} />
                </div>
            )}
            {requestsByModelData && (
                <div className="card" onClick={() => handleCardClick('barChart', requestsByModelData, 'Requests by Model', chartOptions)}>
                    <h3><FaChartBar /> Requests by Model</h3>
                    <Bar data={requestsByModelData} options={chartOptions} />
                </div>
            )}
            {requestsByCategoryData && (
                <div className="card" onClick={() => handleCardClick('doughnutChart', requestsByCategoryData, 'Requests by Category', chartOptions)}>
                    <h3><FaChartPie /> Requests by Category</h3>
                    <Doughnut
                        ref={chartRef}
                        data={requestsByCategoryData}
                        options={{
                            plugins: { legend: { labels: { color: '#333' } } }, // Dark text for legend
                            onClick: handleChartClick
                        }}
                    />
                </div>
            )}

            <Modal isOpen={isModalOpen} onClose={closeModal}>
                {modalContent && (
                    <DetailedCardView
                        type={modalContent.type}
                        data={modalContent.data}
                        title={modalContent.title}
                        chartOptions={modalContent.chartOptions}
                        siteIcons={siteIcons}
                        period={modalContent.period}
                    />
                )}
            </Modal>
        </div>
    );
}

export default Summary;