// chrome-extension/src/content.js

// --- Logger Utility (Integrated) ---
const Logger = {
    debugEnabled: true, // Set to true for debugging, false for production
    init: function(enabled) {
        this.debugEnabled = enabled;
    },
    debug: function(...args) {
        if (this.debugEnabled) {
            console.log("[Content Debug]", ...args);
        }
    },
    info: function(...args) {
        console.log("[Content Info]", ...args);
    },
    error: function(...args) {
        console.error("[Content Error]", ...args);
    }
};

// --- Message Sender (Integrated) ---
const MessageSender = {
    sendCapturedData: function(payload) {
        Logger.info("➡️ [Sending to Background] Preparing to send captured AI data.");
        Logger.debug("Payload being sent:", payload);
        chrome.runtime.sendMessage({
            type: 'CAPTURED_AI_DATA',
            payload: payload
        }, (response) => {
            if (chrome.runtime.lastError) {
                Logger.error("Error sending message to background script:", chrome.runtime.lastError);
            } else {
                if (response.serverResult && response.serverResult.success) {
                    Logger.info(`✅ Data successfully processed by background:`, response.payload);
                } else {
                    Logger.error(`❌ Failed to process data by background:`, response.payload, "Server Result:", response.serverResult);
                }
            }
        });
    }
};

// --- Main Execution ---
Logger.init(true); // Default to true for content scripts for now, can be set by config later
Logger.info("AI Activity Tracker: Content script loaded successfully.");

// Listen for messages from the injected script (inject.js)
window.addEventListener('message', (event) => {
    // Only accept messages from our own window and of type 'AI_API_CALL'
    if (event.source === window && event.data.type === 'AI_API_CALL') {
        Logger.info("⬅️ [Message from Inject.js] Received AI_API_CALL.");
        Logger.debug("Raw data from inject.js:", event.data);

        const { promptText, responseText, conversationId, url, timestamp } = event.data;
        const source = window.location.hostname;

        // Attempt to extract model from URL or requestBody if available
        let model = 'unknown';
        if (url.includes('gpt-4o')) {
            model = 'gpt-4o';
        } else if (url.includes('gpt-4')) {
            model = 'gpt-4';
        } else if (url.includes('gpt-3.5')) {
            model = 'gpt-3.5';
        } else if (url.includes('gemini-pro')) {
            model = 'gemini-pro';
        }
        // Further model extraction can be done from requestBody if needed
        if (event.data.requestBody && event.data.requestBody.model) {
            model = event.data.requestBody.model;
            Logger.info(`[Model Extracted] From requestBody: ${model}`);
        } else {
            Logger.info(`[Model Extracted] From URL: ${model}`);
        }

        // Basic language detection (can be improved)
        let language = 'unknown';
        if (promptText && promptText.trim().length > 0 && /[가-힣]/.test(promptText)) {
            language = 'KO';
            Logger.info("[Language Detected] Korean.");
        } else if (promptText && promptText.trim().length > 0) {
            language = 'EN';
            Logger.info("[Language Detected] English.");
        } else {
            // If promptText is empty or null, default to EN or KO based on preference
            language = 'EN'; // Default language if no prompt text
            Logger.info("[Language Defaulted] Prompt text empty, defaulted to English.");
        }

        const payload = {
            conversationId: conversationId || crypto.randomUUID(), // Use extracted ID or generate new
            prompt: promptText,
            response: responseText,
            model: model,
            source: source,
            timestamp: timestamp,
            language: language
        };

        Logger.info("[Payload Ready] Sending to background script.");
        Logger.debug("Final payload:", payload);
        // Send the full payload to the background script
        MessageSender.sendCapturedData(payload);
    }
});

// Listen for confirmation messages from the background script
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'DATA_PROCESSED') {
        if (message.payload.serverResult && message.payload.serverResult.success) {
            Logger.info(`✅ Data successfully processed by background:`, message.payload.clientData);
        } else {
            Logger.error(`❌ Failed to process data by background:`, message.payload.clientData, "Server Result:", message.payload.serverResult);
        }
        sendResponse({ status: "Acknowledged" });
    }
});
