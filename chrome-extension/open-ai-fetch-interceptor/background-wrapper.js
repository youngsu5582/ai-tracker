// Import logging utility and other scripts (fail fast with clear error)
try {
  importScripts('logger.js');
  importScripts('data-storage.js');
  importScripts('background.js');
} catch (e) {
  console.error('[Background] Failed to load core scripts:', e);
  throw e;
}

// Asynchronously initialize the logger from storage
(async () => {
  const {debugMode} = await chrome.storage.sync.get({debugMode: false});
  Logger.init(debugMode);
  const log = Logger.create('Background');
  log.info(`Logger initialized with debugMode: ${debugMode}`);
})();

// Listen for changes to update the logger dynamically
chrome.storage.onChanged.addListener((changes, namespace) => {
  if (changes.debugMode && namespace === 'sync') {
    const newDebugValue = changes.debugMode.newValue;
    Logger.init(newDebugValue);
    Logger.create('Background').info(
        `Logger debugMode updated to: ${newDebugValue}`);
  }
});