# AI Tracker Project

## 1. Project Overview

This project aims to develop an application that tracks, analyzes, and provides statistics on AI service usage from platforms like Gemini and OpenAI.

## 2. Architecture

The application will be structured as a multi-module project to ensure clear separation of concerns between the different components.

*   **`backend`**: A Spring Boot application responsible for data processing, storage, and providing a REST API.
*   **`frontend`**: A React-based single-page application (SPA) for data visualization and user interaction.
*   **`chrome-extension`**: A browser extension to capture and send prompt data to the backend.

## 3. Tech Stack

*   **Backend**:
    *   Java 21
    *   Spring Boot 3.x
    *   Gradle
*   **Frontend**:
    *   React
    *   (A suitable charting library like Chart.js or Recharts will be chosen later)
*   **Data Storage**:
    *   Initial implementation will use the local file system.
    *   The design will include a data access interface to easily switch to other storage solutions like MongoDB in the future.
*   **Chrome Extension**:
    *   Standard HTML, CSS, JavaScript.

## 4. Core Features

1.  **Chrome Extension**: Captures user prompts and metadata from AI web interfaces (ChatGPT, Gemini, etc.) and sends them to the backend.
2.  **Backend**:
    *   Receives and stores data from the extension.
    *   Uses an AI model (e.g., via an API call to ChatGPT) to analyze and categorize prompts (e.g., "development," "daily life").
    *   Aggregates data for statistical analysis (e.g., request counts by site, model, category).
3.  **Frontend**:
    *   Displays statistical data through a dashboard, as shown in the reference image.

## 5. Code Style

*   Adherence to Object-Oriented Programming (OOP) and Clean Code principles.
*   Maintain code quality through appropriate unit testing.
