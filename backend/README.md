# AI Activity Tracker - Backend Module

## 1. Overview

This module is the Spring Boot backend for the AI Activity Tracker application. It is responsible for receiving prompt data from the Chrome extension, processing it (including AI-based categorization), storing it in MongoDB, and providing REST APIs for the frontend application.

## 2. Features

*   **Data Ingestion**: Receives prompt data (prompt text, model, source, timestamp, language) from the Chrome extension via a REST API endpoint.
*   **AI Categorization**: Integrates with the OpenAI API to categorize prompts into predefined categories (e.g., "Software Development", "Daily Life"). Supports multi-language categorization based on the provided language.
*   **MongoDB Integration**: Persists prompt data into a MongoDB database using Spring Data MongoDB.
*   **Statistical APIs**: Provides endpoints to retrieve aggregated usage statistics (total requests, requests by site, model, category, and hourly distribution) for daily, weekly, and monthly periods.
*   **History and Detail APIs**: Offers endpoints to fetch historical prompt data by date and to retrieve all prompts belonging to a specific category.

## 3. Tech Stack

*   **Language**: Java 21
*   **Framework**: Spring Boot 3.x
*   **Build Tool**: Gradle
*   **Database**: MongoDB (Spring Data MongoDB)
*   **External API**: OpenAI API (for prompt categorization)
*   **Dependencies**: `spring-boot-starter-web`, `spring-boot-starter-webflux`, `spring-boot-starter-data-mongodb`, `jackson-datatype-jsr310`, `lombok`.

## 4. Setup and Running

### Prerequisites

*   JDK 21 or higher
*   Gradle
*   Running MongoDB instance (preferably via Docker Compose as described in the [main README.md](../README.md))
*   OpenAI API Key

### Configuration

1.  **OpenAI API Key**: Open `src/main/resources/application.properties` and set your OpenAI API key:
    ```properties
    openai.api.key=sk-YOUR_ACTUAL_OPENAI_API_KEY
    ```
2.  **MongoDB Connection**: Ensure your MongoDB connection URI is correctly configured in `src/main/resources/application.properties`. If you are using the Docker Compose setup from the main project, it should be:
    ```properties
    spring.data.mongodb.uri=mongodb://admin:password@localhost:27017/ai_tracker?authSource=admin
    ```

### Running the Application

From the project root directory, execute:

```bash
./gradlew :backend:bootRun &
```

The backend will start on `http://localhost:11240`.

## 5. API Endpoints

*   **POST /api/data/capture**
    *   **Description**: Captures prompt data sent from the Chrome extension.
    *   **Request Body**: `CaptureRequest` DTO (JSON) - `prompt`, `model`, `source`, `timestamp`, `language` (optional, defaults to `KO`).

*   **GET /api/data/statistics**
    *   **Description**: Retrieves aggregated usage statistics.
    *   **Query Parameters**: `period` (optional, `daily`, `weekly`, `monthly` - defaults to `daily`).
    *   **Response**: JSON object containing `totalRequests`, `requestsBySite`, `requestsByModel`, `requestsByCategory`, `topSites`, `topModels`, `requestsByHour`, `categoryTree`.

*   **GET /api/prompts**
    *   **Description**: Retrieves prompts filtered by category.
    *   **Query Parameters**: `category` (required).
    *   **Response**: List of `Prompt` objects.

*   **GET /api/prompts/history**
    *   **Description**: Retrieves historical prompt data for a specific date.
    *   **Query Parameters**: `date` (optional, defaults to today's date in `YYYY-MM-DD` format).
    *   **Response**: List of `Prompt` objects.

## 6. Data Model (`Prompt`)

```java
public record Prompt(
    String id,
    String prompt,
    String model,
    String source,
    LocalDateTime timestamp,
    String category,
    Language language
) {
}
```

## 7. Contributing

Refer to the [main project README.md](../README.md) for general contribution guidelines.
