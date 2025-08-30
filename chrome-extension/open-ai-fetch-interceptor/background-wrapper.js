// chrome-extension/src/background-wrapper.js

// Import logging utility and other scripts
importScripts('logger.js');
importScripts('data-storage.js');
importScripts('background.js');

// Asynchronously initialize the logger from storage
(async () => {
  const { debugMode } = await chrome.storage.sync.get({ debugMode: true });
  Logger.init(debugMode);
  console.log(`[Background] Logger initialized with debugMode: ${debugMode}`);
})();

// Listen for changes to update the logger dynamically
chrome.storage.onChanged.addListener((changes, namespace) => {
  if (changes.debugMode && namespace === 'sync') {
    const newDebugValue = changes.debugMode.newValue;
    Logger.init(newDebugValue);
    console.log(`[Background] Logger debugMode updated to: ${newDebugValue}`);
  }
});