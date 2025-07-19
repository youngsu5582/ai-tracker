// chrome-extension/src/logger.js

/**
 * Simple logging utility that respects a debug flag.
 * Logs messages to the console only if debugging is enabled.
 */
const Logger = {
    debugEnabled: false,

    /**
     * Initializes the logger with the debug status.
     * @param {boolean} enabled - True to enable debug logging, false otherwise.
     */
    init: function(enabled) {
        this.debugEnabled = enabled;
    },

    /**
     * Logs a debug message.
     * @param {...any} args - Arguments to log.
     */
    debug: function(...args) {
        if (this.debugEnabled) {
            console.log("[Debug]", ...args);
        }
    },

    /**
     * Logs an info message.
     * @param {...any} args - Arguments to log.
     */
    info: function(...args) {
        console.log("[Info]", ...args);
    },

    /**
     * Logs an error message.
     * @param {...any} args - Arguments to log.
     */
    error: function(...args) {
        console.error("[Error]", ...args);
    }
};
