# AI Activity Tracker

## 1. Project Overview

The AI Activity Tracker is a multi-module application designed to track, analyze, and provide statistics on AI service usage from platforms like ChatGPT and Gemini. It consists of a Chrome extension to capture prompt data, a Spring Boot backend for data processing and storage, and a React-based frontend for data visualization.

## 2. Features

*   **Chrome Extension**:
    *   Captures user prompts, model names, and timestamps from AI web interfaces (currently ChatGPT and Gemini).
    *   Sends captured data to the backend server.
    *   Supports dynamic detection of AI platforms for easy extensibility.
    *   Includes robust event handling for various input methods (typing, copy-paste).
    *   Configurable debug logging.

*   **Backend (Spring Boot)**:
    *   Receives and stores prompt data from the Chrome extension.
    *   Utilizes OpenAI API to categorize prompts (e.g., "Software Development", "Daily Life").
    *   Supports multi-language categorization (English and Korean).
    *   Provides REST APIs for:
        *   Capturing new prompts.
        *   Retrieving usage statistics (daily, weekly, monthly).
        *   Fetching historical prompt data by date.
        *   Retrieving prompts filtered by category.

*   **Frontend (React)**:
    *   Interactive dashboard for visualizing AI usage statistics.
    *   Displays total requests, requests by site, model, and category.
    *   Includes a timeline chart for hourly request distribution.
    *   Allows switching between daily, weekly, and monthly statistics.
    *   Provides a history page to view past prompts by date.
    *   Enables drilling down from category charts to view specific prompts within that category.
    *   Modern, dark-themed UI.

## 3. Architecture


The application is structured as a multi-module project:

*   `backend`: A Spring Boot application (Java 21, Gradle) providing the REST API and data processing.
*   `frontend`: A React single-page application for data visualization.
*   `chrome-extension`: A browser extension (HTML, CSS, JavaScript) for data capture.

## 4. Tech Stack

*   **Backend**: Java 21, Spring Boot 3.x, Gradle, Spring Data MongoDB, OpenAI API (for categorization).
*   **Frontend**: React, Chart.js, React Router, Axios.
*   **Data Storage**: MongoDB (via Docker Compose for local development).
*   **Chrome Extension**: Standard HTML, CSS, JavaScript.

## 5. Setup and Running the Project

### Prerequisites

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK) 21 or higher**
*   **Gradle** (usually bundled with Spring Boot projects, but ensure it's accessible)
*   **Node.js and npm** (or yarn)
*   **Docker and Docker Compose** (for running MongoDB)
*   **Google Chrome** (or any Chromium-based browser for the extension)
*   **An OpenAI API Key**: You'll need this for prompt categorization.

### Backend Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/youngsu5582/ai-tracker.git
    ```
2.  **Configure OpenAI API Key**:
    Open `backend/src/main/resources/application.yml` and replace `sk-YOUR_ACTUAL_OPENAI_API_KEY` with your actual OpenAI API Key:
    ```yaml
    openai:
      api:
        key: sk-YOUR_ACTUAL_OPENAI_API_KEY
    ```
3.  **Start MongoDB with Docker Compose**:
    Navigate to the project root directory (where `docker-compose.yml` is located) and run:
    ```bash
    docker-compose up -d
    ```
    This will start a MongoDB container and a Mongo Express (web-based GUI for MongoDB) container.
    *   MongoDB will be accessible at `mongodb://localhost:27017`.
    *   Mongo Express will be accessible at `http://localhost:8081` (username: `admin`, password: `password`).

4.  **Run the Backend Application**:
    Navigate to the project root directory and run the Spring Boot application:
    ```bash
    ./gradlew :backend:bootRun &
    ```
    The backend will start on `http://localhost:11240`.

### Frontend Setup

1.  **Install Dependencies**:
    Navigate to the `frontend` directory and install the Node.js dependencies:
    ```bash
    cd frontend
    npm install
    ```
2.  **Configure API Base URL**:
    The frontend automatically reads the API base URL from the `.env` file. Ensure `frontend/.env` contains:
    ```
    REACT_APP_API_BASE_URL=http://localhost:11240/api
    ```
3.  **Run the Frontend Application**:
    From the `frontend` directory, run the development server:
    ```bash
    npm start &
    ```
    The frontend application will be accessible at `http://localhost:3000`.

### Chrome Extension Setup

1.  **Load the Extension in Chrome**:
    *   Open Chrome and navigate to `chrome://extensions`.
    *   Enable "Developer mode" (top right corner).
    *   Click "Load unpacked" (top left corner).
    *   Select the `chrome-extension/open-ai-fetch-interceptor` directory from your project.
2.  **Configure API Endpoint**:
    *   Right-click the extension's icon in the Chrome toolbar and select "Options".
    *   In the options page, enter the full URL for your backend API endpoint (e.g., `http://localhost:11240/api/data/capture`).
    *   Click "Save".
3.  **Reload the Extension**:
    After any changes to the extension files, go back to `chrome://extensions` and click the "Reload" button (circular arrow icon) for the extension.

## 6. Data Storage

Prompt data is stored in a MongoDB database. The backend connects to a MongoDB instance, which is conveniently run via Docker Compose for local development.

*   **Database Name**: `ai_tracker`
*   **Collection Name**: `prompts`
*   **Connection URI (for backend)**: `mongodb://admin:password@localhost:27017/ai_tracker?authSource=admin`

You can view the data directly using:
*   **Mongo Express**: Access `http://localhost:8081` in your browser (username: `admin`, password: `password`).
*   **MongoDB Compass**: Connect using the provided URI.
*   **MongoDB Shell**: `docker exec -it ai_tracker_mongodb mongosh -u admin -p password --authenticationDatabase admin`

## 7. Usage

1.  **Start all services**: Ensure MongoDB, backend, and frontend are running, and the Chrome extension is loaded.
2.  **Use AI Services**: Navigate to supported AI platforms (e.g., `chat.openai.com`, `chatgpt.com`, `gemini.google.com`).
3.  **Enter Prompts**: Type your prompts and send them as usual. The extension will capture the data in the background.
4.  **View Statistics**: Open your browser and go to `http://localhost:3000`.
    *   The default page is the **History** view, showing your daily prompts.
    *   Click "Dashboard" in the header to see usage statistics.
    *   On the Dashboard, you can switch between Daily, Weekly, and Monthly views.
    *   Click on a category in the "Requests by Category" chart to see a list of prompts for that category.

## 8. Contributing

Feel free to fork the repository, open issues, or submit pull requests.