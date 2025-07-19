# AI Activity Tracker - Chrome Extension Module

## 1. Overview

This module is the Chrome browser extension for the AI Activity Tracker application. Its primary function is to capture user interactions (prompts, model usage) on supported AI web interfaces and send this data to the backend server for analysis and storage.

## 2. Features

*   **Prompt Capture**: Automatically detects and captures user-entered prompts from AI chat interfaces.
*   **Model Detection**: Identifies the AI model being used for the prompt.
*   **Multi-Platform Support**: Configured to work with popular AI platforms like ChatGPT (`chat.openai.com`, `chatgpt.com`) and Gemini (`gemini.google.com`). Easily extensible to support new platforms.
*   **Robust Event Handling**: Captures prompts reliably across various user input methods (typing, copy-paste, Enter key, send button clicks).
*   **Configurable Logging**: Includes a flexible logging system (`Logger.js`) that allows enabling/disabling debug logs via a configuration flag.
*   **Modular Design**: Code is organized into separate, reusable modules for better maintainability.

## 3. Tech Stack

*   **Core Technologies**: Standard HTML, CSS, JavaScript.
*   **Manifest Version**: Manifest V3.

## 4. Setup and Running

### Prerequisites

*   Google Chrome (or any Chromium-based browser).
*   The backend application must be running (refer to the [main README.md](../../README.md) for backend setup).

### Configuration

The backend API endpoint and debug mode are configured in `src/config.js`:

```javascript
// chrome-extension/src/config.js
const config = {
    API_ENDPOINT: "http://localhost:11240/api/data/capture", // Your backend API endpoint
    DEBUG_MODE: true // Set to true for development, false for production
};
```

### Loading the Extension in Chrome

1.  **Open Chrome Extensions Page**:
    *   Open your Chrome browser.
    *   Type `chrome://extensions` in the address bar and press Enter.
2.  **Enable Developer Mode**:
    *   In the top right corner of the Extensions page, toggle on "Developer mode".
3.  **Load Unpacked Extension**:
    *   Click the "Load unpacked" button (usually in the top left corner).
    *   A file dialog will appear. Navigate to your project directory and select the `chrome-extension` folder.
4.  **Verify Loading**:
    *   The "AI Activity Tracker" extension should now appear in your list of installed extensions.

### Reloading the Extension

After making any changes to the extension's source code, you must reload the extension for the changes to take effect:

1.  Go back to `chrome://extensions`.
2.  Find the "AI Activity Tracker" extension.
3.  Click the "Reload" button (a circular arrow icon) on the extension's card.

## 5. Project Structure

*   `src/`: Contains all JavaScript source files.
    *   `background-wrapper.js`: Entry point for the background service worker, loads other scripts.
    *   `background.js`: Handles communication with the backend and message passing.
    *   `content.js`: Injected into AI service web pages to capture user interactions.
    *   `config.js`: Global configuration settings (e.g., API endpoint, debug mode).
    *   `logger.js`: Utility for controlled console logging.
    *   `message-sender.js`: Helper for sending messages to the background script.
    *   `platform-config.js`: Defines selectors and logic for different AI platforms (e.g., ChatGPT, Gemini).
*   `manifest.json`: The manifest file for the Chrome extension, defining its properties, permissions, and scripts.

## 6. Supported Platforms

Currently configured to capture data from:

*   `chat.openai.com`
*   `chatgpt.com`
*   `gemini.google.com`

To add support for a new platform, you would typically:
1.  Add its hostname and corresponding selectors/logic to `src/platform-config.js`.
2.  Update `manifest.json` to include the new platform's domain in `host_permissions` and `content_scripts` `matches`.

## 7. Debugging

To enable detailed debug logs in the browser's console:
1.  Open `src/config.js`.
2.  Set `DEBUG_MODE: true`.
3.  Reload the extension from `chrome://extensions`.

Then, open the developer console (F12) on the AI service web page or for the extension's background page to see the logs.

## 8. Contributing

Refer to the [main project README.md](../../README.md) for general contribution guidelines.
