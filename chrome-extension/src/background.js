// chrome-extension/src/background.js

/**
 * Background script for the AI Activity Tracker extension.
 * Handles messages from content scripts and sends data to the backend server.
 */

async function sendToServer(payload) {
    Logger.debug("Attempting to send data to server:", payload);
    try {
        const response = await fetch(config.API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (response.ok) {
            Logger.info("Successfully sent data to the server.");
            return { success: true, status: response.status };
        } else {
            const errorText = await response.text();
            Logger.error("Server returned an error:", response.status, errorText);
            return { success: false, status: response.status, error: errorText };
        }
    } catch (error) {
        Logger.error("Failed to send data to the server:", error);
        return { success: false, error: error.message };
    }
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'PROMPT_CAPTURED') {
        Logger.info("--- Prompt Received by Background ---", message.payload);

        // Asynchronously send data to the server
        sendToServer(message.payload).then(serverResult => {
            // Send confirmation back to the content script, including server status
            if (sender.tab && sender.tab.id) {
                chrome.tabs.sendMessage(sender.tab.id, {
                    type: 'DATA_PROCESSED',
                    payload: {
                        clientData: message.payload,
                        serverResult: serverResult
                    }
                });
            }
        });
    }
    // Return true to indicate you will send a response asynchronously
    return true;
});