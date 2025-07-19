
(() => {
  console.log('AI Activity Tracker: Page script injecting (v4)...');

  // --- Helper to dispatch events ---
  function dispatchData(type, payload) {
    window.dispatchEvent(new CustomEvent('FROM_PAGE_SCRIPT', { detail: { type, payload } }));
  }

  // --- 1. Intercept fetch API ---
  const originalFetch = window.fetch;
  window.fetch = async (input, init) => {
    const url = typeof input === 'string' ? input : input.url;
    if (url.includes('/backend-api/conversation')) {
        // This will likely handle response streams, less likely the initial POST
    }
    const response = await originalFetch(input, init);
    // Stream handling logic can remain here if needed, but XHR is more likely for the main request.
    return response;
  };

  // --- 2. Intercept XMLHttpRequest API (more likely to capture the prompt) ---
  const originalXhrOpen = XMLHttpRequest.prototype.open;
  const originalXhrSend = XMLHttpRequest.prototype.send;

  let requestInfo = { method: null, url: null };

  XMLHttpRequest.prototype.open = function(method, url) {
    // Store the method and url to use them in the .send() override
    requestInfo = { method, url };
    return originalXhrOpen.apply(this, arguments);
  };

  XMLHttpRequest.prototype.send = function(body) {
    if (requestInfo.url && typeof requestInfo.url === 'string' && requestInfo.url.includes('/backend-api/conversation')) {
      console.log(`[Tracker-XHR] Intercepted ${requestInfo.method} to ${requestInfo.url}`);
      console.log(`[Tracker-XHR] Request body:`, body);

      try {
        const data = JSON.parse(body);
        const userMessage = data.messages?.[0]?.content?.parts?.[0];
        const model = data.model;

        if (userMessage && model) {
          console.log("[Tracker-XHR] Parsed prompt:", userMessage);
          console.log("[Tracker-XHR] Parsed model:", model);
          dispatchData('PROMPT_DATA', {
            prompt: userMessage,
            model: model,
            timestamp: new Date().toISOString(),
            source: window.location.hostname,
          });
        }
      } catch (e) {
        console.error('[Tracker-XHR] Error parsing request body:', e);
      }

      // --- Intercept the response ---
      this.addEventListener('load', function() {
        console.log('[Tracker-XHR] Request finished. Status:', this.status);
        try {
          // SSE responses are handled differently, but if it's a simple JSON response:
          if (this.responseText) {
            // This part is tricky because the response is a stream.
            // The final, complete response might not be available here.
            // We will rely on the fetch interceptor for the stream, 
            // but log here for debugging.
            console.log('[Tracker-XHR] Response Text:', this.responseText.substring(0, 500));
          }
        } catch (e) {
          console.error('[Tracker-XHR] Error reading response text:', e);
        }
      });
    }
    return originalXhrSend.apply(this, arguments);
  };

  console.log('AI Activity Tracker: Page script injected and APIs overridden (v4).');
})();
