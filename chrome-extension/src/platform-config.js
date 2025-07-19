// chrome-extension/src/platform-config.js

/**
 * Defines platform-specific configurations for AI services.
 * Each key is a hostname, and its value is an object containing selectors and logic
 * specific to that platform's UI.
 */
const platformConfigs = {
    "chat.openai.com": {
        promptTextareaSelector: '#prompt-textarea',
        sendButtonSelector: '#composer-submit-button',
        modelNameExtractor: (doc) => {
            const modelSwitcher = doc.querySelector('button[data-testid^="model-switcher-"]'); // More general selector
            if (!modelSwitcher) return 'unknown';
            // Try to find the model name in common places, e.g., a span or div with text content
            const modelNameElement = modelSwitcher.querySelector('span, div');
            return modelNameElement ? modelNameElement.textContent.trim() : 'unknown';
        },
        getPromptText: (element) => element.textContent || '',
        preventDefaultOnEnter: true
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
