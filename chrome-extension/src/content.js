// chrome-extension/src/content.js

// --- Logger Utility (Integrated) ---
const Logger = {
    debugEnabled: false,
    init: function(enabled) {
        this.debugEnabled = enabled;
    },
    debug: function(...args) {
        if (this.debugEnabled) {
            console.log("[Debug]", ...args);
        }
    },
    info: function(...args) {
        console.log("[Info]", ...args);
    },
    error: function(...args) {
        console.error("[Error]", ...args);
    }
};

// --- Platform Configurations (Integrated) ---
const platformConfigs = {
    "chat.openai.com": {
        promptTextareaSelector: '#prompt-textarea',
        sendButtonSelector: '#composer-submit-button',
        modelNameExtractor: (doc) => {
            const modelSwitcher = doc.querySelector('button[data-testid^="model-switcher-"]');
            if (!modelSwitcher) return 'unknown';
            const modelNameElement = modelSwitcher.querySelector('span, div');
            return modelNameElement ? modelNameElement.textContent.trim() : 'unknown';
        },
        getPromptText: (element) => element.textContent || '',
        preventDefaultOnEnter: false // Changed to false
    },
    "chatgpt.com": {
        promptTextareaSelector: '#prompt-textarea',
        sendButtonSelector: '#composer-submit-button',
        modelNameExtractor: (doc) => {
            const modelSwitcher = doc.querySelector('button[data-testid^="model-switcher-"]');
            if (!modelSwitcher) return 'unknown';
            const modelNameElement = modelSwitcher.querySelector('span, div');
            return modelNameElement ? modelNameElement.textContent.trim() : 'unknown';
        },
        getPromptText: (element) => element.textContent || '',
        preventDefaultOnEnter: false // Changed to false
    },
    "gemini.google.com": {
        promptTextareaSelector: 'div[contenteditable="true"][aria-label="여기에 프롬프트 입력"]',
        sendButtonSelector: '#app-root > main > side-navigation-v2 > mat-sidenav-container > mat-sidenav-content > div > div.content-container > chat-window > div > input-container > div > input-area-v2 > div > div > div.trailing-actions-wrapper.ng-tns-c1076557939-6 > div > div.mat-mdc-tooltip-trigger.send-button-container.ng-tns-c1076557939-6.inner.is-mobile-device.ng-star-inserted.visible > button',
        modelNameExtractor: (doc) => {
            const modelSwitcher = doc.querySelector('#app-root > main > div > bard-mode-switcher > div > button > span.mdc-button__label > div');
            if (!modelSwitcher) return 'unknown';
            const modelSpan = modelSwitcher.querySelector('span');
            return modelSpan ? modelSpan.textContent.trim() : 'unknown';
        },
        getPromptText: (element) => element.textContent || '',
        preventDefaultOnEnter: false
    }
};

// --- Message Sender (Integrated) ---
const MessageSender = {
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

// --- Prompt Capture Manager (Integrated) ---
const PromptCaptureManager = {
    currentPromptText: '',
    currentPlatformConfig: null,
    intervalId: null,

    init: function(platformConfigs) {
        const hostname = window.location.hostname;
        if (platformConfigs[hostname]) {
            this.currentPlatformConfig = platformConfigs[hostname];
            Logger.info(`Detected platform: ${hostname}. Successfully extracted site information.`);
        } else {
            Logger.info(`Unknown platform: ${hostname}. AI Activity Tracker may not function correctly.`);
            return;
        }

        Logger.debug("Starting interval to find UI elements...");
        this.intervalId = setInterval(() => {
            const promptTextarea = document.querySelector(this.currentPlatformConfig.promptTextareaSelector);
            if (promptTextarea) {
                Logger.info("UI is ready. Attaching listeners.");
                clearInterval(this.intervalId);
                this.attachListeners(promptTextarea);
            } else {
                Logger.debug("UI not ready yet, still searching for prompt textarea...");
            }
        }, 1000);
    },

    attachListeners: function(promptTextarea) {
        const updatePromptText = (e) => {
            setTimeout(() => {
                const newText = this.currentPlatformConfig.getPromptText(e.target);
                if (newText !== this.currentPromptText) {
                    this.currentPromptText = newText;
                    Logger.debug(`Prompt text updated via '${e.type}' event. New text: '${this.currentPromptText}'`);
                }
            }, 0);
        };

        Logger.debug("Attaching comprehensive listeners (input, paste, cut) to prompt textarea.");
        promptTextarea.addEventListener('input', updatePromptText);
        promptTextarea.addEventListener('paste', updatePromptText);
        promptTextarea.addEventListener('cut', updatePromptText);

        Logger.debug("Attaching 'keydown' listener for Enter key.");
        promptTextarea.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                Logger.debug("Enter key pressed without Shift.");
                if (this.currentPlatformConfig.preventDefaultOnEnter) {
                    e.preventDefault();
                }
                this.sendCapturedPrompt();
            }
        });

        Logger.debug("Attaching 'click' listener to document body for send button.");
        document.body.addEventListener('click', (e) => {
            const sendButton = e.target.closest(this.currentPlatformConfig.sendButtonSelector);
            if (sendButton) {
                Logger.debug("Send button clicked:", sendButton);
                this.sendCapturedPrompt();
            }
        }, true);
    },

    sendCapturedPrompt: function() {
        Logger.debug("sendCapturedPrompt function called.");
        Logger.debug(`Using prompt text from variable: '${this.currentPromptText}'`);

        if (!this.currentPromptText || this.currentPromptText.trim() === '') {
            Logger.debug("Prompt text is empty. Aborting send.");
            return;
        }

        const modelName = this.getModelName();
        Logger.info(`Capturing... Model: ${modelName}, Prompt: ${this.currentPromptText}`);

        MessageSender.sendCapturedPrompt({
            prompt: this.currentPromptText,
            model: modelName,
            timestamp: new Date().toISOString(),
            source: window.location.hostname
        });

        this.currentPromptText = '';
        Logger.debug("Prompt text variable cleared after sending.");
    },

    getModelName: function() {
        return this.currentPlatformConfig.modelNameExtractor(document);
    }
};

// --- Main Execution ---
// Initialize the logger with the debug mode from config
// Note: config object is not directly available in content script, so we assume DEBUG_MODE is false by default
// For production, this should be false. For debugging, you can manually set Logger.debugEnabled = true in console.
Logger.init(false); // Default to false for content scripts

Logger.info("AI Activity Tracker: Content script loaded successfully.");

// Initialize the PromptCaptureManager with platform configurations
PromptCaptureManager.init(platformConfigs);

// Listen for confirmation messages from the background script
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'DATA_PROCESSED') {
        Logger.info(`✅ Data successfully processed by background:`, message.payload);
        sendResponse({ status: "Acknowledged" });
    }
});