// Asynchronously initialize the Logger's global state first.
(async () => {
  const { debugMode } = await chrome.storage.sync.get({ debugMode: true });
  Logger.init(debugMode); // Call init on the main Logger object
  // Use console.log for this meta-logging as the logger instance isn't fully configured yet.
  console.log(`[Content] Logger initialized with debugMode: ${debugMode}`);
})();

// Listen for changes to update the logger's global state.
chrome.storage.onChanged.addListener((changes, namespace) => {
  if (changes.debugMode && namespace === 'sync') {
    const newDebugValue = changes.debugMode.newValue;
    Logger.init(newDebugValue); // Call init on the main Logger object
    console.log(`[Content] Logger debugMode updated to: ${newDebugValue}`);
  }
});

// Now, create a logger instance for this file's context.
const logger = Logger.create();

// Listen for messages from the main world script (inject.js)
window.addEventListener('message', (event) => {
  // Only accept messages from our own window and with a specific type
  if (event.source === window && event.data && event.data.type
      === 'FETCH_INTERCEPTED') {
    logger.debug("Received message from MainWorld:", event.data);
    // Send the data to the background script
    chrome.runtime.sendMessage(event.data);
  }
});

logger.info("Content script loaded and listening.");
