// chrome-extension/src/background-wrapper.js

// Import configuration and logging utility first
importScripts('config.js');
importScripts('logger.js');

// Initialize the logger with the debug mode from config
Logger.init(config.DEBUG_MODE);

// Import other modules
importScripts('message-sender.js');
importScripts('background.js');