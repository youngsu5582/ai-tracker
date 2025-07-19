# AI Activity Tracker - Frontend Module

## 1. Overview

This module is the React-based frontend for the AI Activity Tracker application. It provides an interactive dashboard and a history view to visualize AI service usage statistics and prompt history.

## 2. Features

*   **Dashboard View**: Displays various charts and statistics including total requests, requests by site, model, category, and hourly distribution.
*   **History View**: Shows a chronological timeline of prompts sent, grouped by date.
*   **Period Selection**: Allows users to switch between daily, weekly, and monthly statistics on the dashboard.
*   **Category Drill-down**: Users can click on a category in the dashboard charts to view a detailed list of prompts belonging to that category.
*   **Responsive UI**: Designed to be user-friendly across different screen sizes.
*   **Modern Design**: Features a dark-themed interface for better readability and aesthetics.

## 3. Tech Stack

*   **Framework**: React
*   **Charting Library**: Chart.js with `react-chartjs-2`
*   **Routing**: `react-router-dom`
*   **HTTP Client**: Axios
*   **Styling**: Pure CSS

## 4. Setup and Running

### Prerequisites

*   Node.js and npm (or yarn)
*   The backend application must be running (refer to the [main README.md](../README.md) for backend setup).

### Configuration

The API base URL for the backend is configured via an environment variable. Ensure your `.env` file in the `frontend` directory contains:

```
REACT_APP_API_BASE_URL=http://localhost:11240/api
```

### Running the Application

1.  Navigate to the `frontend` directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm start &
    ```

The frontend application will be accessible at `http://localhost:3000`.

## 5. Project Structure

*   `src/App.js`: Main application component, handles routing and overall layout.
*   `src/pages/`: Contains main page components (`Dashboard.js`, `History.js`, `PromptList.js`).
*   `src/components/`: Contains reusable UI components (`Summary.js`, `TopList.js`, `TimelineChart.js`, `CategoryTree.js`).
*   `src/App.css`: Global styles for the application.

## 6. API Interactions

The frontend interacts with the backend API at `http://localhost:11240/api` (configurable via `REACT_APP_API_BASE_URL`).

*   **GET /api/data/statistics?period={daily|weekly|monthly}**: Fetches usage statistics.
*   **GET /api/prompts/history?date={YYYY-MM-DD}**: Fetches prompts for a specific date.
*   **GET /api/prompts?category={categoryName}**: Fetches prompts for a specific category.

## 7. Contributing

Refer to the [main project README.md](../README.md) for general contribution guidelines.
