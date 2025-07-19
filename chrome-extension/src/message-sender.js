// chrome-extension/src/message-sender.js

/**
 * Handles sending messages to the background script.
 */
const MessageSender = {
    /**
     * Sends a captured prompt payload to the background script.
     * @param {object} payload - The data payload to send.
     */
    sendCapturedPrompt: function(payload) {
        chrome.runtime.sendMessage({
            type: 'PROMPT_CAPTURED',
            payload: payload
        }, (response) => {
            if (chrome.runtime.lastError) {
                Logger.error("Error sending message to background script:", chrome.runtime.lastError);
            } else {
                Logger.debug("Message sent to background script, response:", response);
            }
        });
    }
};
