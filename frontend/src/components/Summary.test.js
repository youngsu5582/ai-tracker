import { render, screen } from '@testing-library/react';
import Summary from './Summary';

// Mock axios to prevent actual API calls during tests
jest.mock('axios');

// Mock react-router-dom's useNavigate
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => jest.fn(),
}));

// Mock react-chartjs-2 components to avoid rendering issues in tests
jest.mock('react-chartjs-2', () => ({
  Bar: () => null,
  Doughnut: () => null,
  getElementAtEvent: () => [], // Mock this function as well
}));

// Mock chart.js to prevent errors related to canvas rendering
jest.mock('chart.js', () => ({
  Chart: { register: jest.fn() },
  CategoryScale: {}, LinearScale: {}, BarElement: {}, Title: {}, Tooltip: {}, Legend: {}, ArcElement: {}
}));


describe('Summary Component', () => {
  const mockStats = {
    totalRequests: 123,
    topSites: { 'chatgpt.com': 50, 'gemini.google.com': 73 },
    topModels: { 'gpt-4': 60, 'gemini-pro': 63 },
    requestsByHour: { '00': 5, '01': 10 },
    categoryTree: { 'Development': 80, 'Daily Life': 43 },
    requestsBySite: { 'chatgpt.com': 50, 'gemini.google.com': 73 },
    requestsByModel: { 'gpt-4': 60, 'gemini-pro': 63 },
    requestsByCategory: { 'Development': 80, 'Daily Life': 43 },
  };

  test('renders total requests', () => {
    render(<Summary stats={mockStats} period="daily" />);
    expect(screen.getByText(/Total Requests/i)).toBeInTheDocument();
    expect(screen.getByText(/123/)).toBeInTheDocument();
  });

  test('renders Top Sites', () => {
    render(<Summary stats={mockStats} period="daily" />);
    expect(screen.getByText(/Top Sites/i)).toBeInTheDocument();
    expect(screen.getByText(/chatgpt.com/i)).toBeInTheDocument();
  });

  test('renders Timeline (by Hour) for daily period', () => {
    render(<Summary stats={mockStats} period="daily" />);
    expect(screen.getByText(/Timeline \(by Hour\)/i)).toBeInTheDocument();
  });

  test('renders Timeline (by Day) for weekly period', () => {
    render(<Summary stats={mockStats} period="weekly" />);
    expect(screen.getByText(/Timeline \(by Day\)/i)).toBeInTheDocument();
  });

  test('renders Requests by Site chart', () => {
    render(<Summary stats={mockStats} period="daily" />);
    expect(screen.getByText(/Requests by Site/i)).toBeInTheDocument();
  });

  test('renders Requests by Model chart', () => {
    render(<Summary stats={mockStats} period="daily" />);
    expect(screen.getByText(/Requests by Model/i)).toBeInTheDocument();
  });

  test('renders Requests by Category chart', () => {
    render(<Summary stats={mockStats} period="daily" />);
    expect(screen.getByText(/Requests by Category/i)).toBeInTheDocument();
  });
});
