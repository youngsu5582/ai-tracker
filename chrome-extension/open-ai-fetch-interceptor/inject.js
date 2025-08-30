// This content script runs at document_start and injects the main world script.

(function () {
  // 'logger' is already declared in content.js
  injectScript(logger, 'open-ai-fetch-interceptor/fetch_interceptor.js');
})();

function injectScript(logger, filePath) {
  const script = document.createElement('script');
  script.src = chrome.runtime.getURL(filePath);
  (document.head || document.documentElement).appendChild(script);
  script.onload = function () {
    this.remove();
  };
  logger.info(
      `Fetch interceptor script injected. path:${filePath}`);
}