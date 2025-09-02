// This content script runs at document_start and injects the main world script.

(function () {
  // 'logger' is already declared in content.js
  const safeLogger = (typeof logger !== 'undefined' && logger) ? logger : {
    debug: (...a) => console.debug('[inject]', ...a),
    info: (...a) => console.log('[inject]', ...a),
    warn: (...a) => console.warn('[inject]', ...a),
    error: (...a) => console.error('[inject]', ...a),
  };
  injectScript(safeLogger,
      'open-ai-fetch-interceptor/fetch_interceptor.js',
      'open-ai-fetch-interceptor-script');
})();

function injectScript(logger, filePath, markerId) {
  const SENTINEL = '__openAiFetchInterceptorInjected';
  if (window[SENTINEL]) {
    logger.warn('Fetch interceptor already injected. Skipping.');
    return;
  }
  if (document.getElementById(markerId)) {
    logger.warn('Fetch interceptor already injected. Skipping.');
    return;
  }
  window[SENTINEL] = true;
  const script = document.createElement('script');
  script.src = chrome.runtime.getURL(filePath);
  script.id = markerId;
  (document.head || document.documentElement).appendChild(script);
  script.onload = function () {
    this.remove();
  };
  script.onerror = function () {
    logger.error(`Failed to inject fetch interceptor. path:${filePath}`, e);
    this.remove();
  }
  logger.info(
      `Fetch interceptor script injected. path:${filePath}`);
}