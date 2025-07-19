// chrome-extension/src/prompt-capture-manager.js

/**
 * Manages the capture of prompts and attachment of event listeners
 * based on the current platform's configuration.
 */
const PromptCaptureManager = {
    currentPromptText: '',
    currentPlatformConfig: null,
    intervalId: null,

    /**
     * Initializes the manager by detecting the platform and attaching listeners.
     * @param {object} platformConfigs - Object containing configurations for different platforms.
     */
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

    /**
     * Attaches event listeners to the prompt textarea and send button.
     * @param {HTMLElement} promptTextarea - The prompt input element.
     */
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

    /**
     * Captures the current prompt and sends it to the background script.
     */
    sendCapturedPrompt: function() {
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

    /**
     * Extracts the model name using the platform-specific extractor.
     * @returns {string} The extracted model name.
     */
    getModelName: function() {
        return this.currentPlatformConfig.modelNameExtractor(document);
    }
};
