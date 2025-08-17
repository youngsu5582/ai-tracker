console.log('Gemini Fetch Interceptor: Main world script (version 0.1.7) loaded and executing.');

// --- Intercept window.fetch ---
const originalFetch = window.fetch;

window.fetch = function() {
  const url = arguments[0];
  const args = Array.from(arguments);

  if (url.includes('https://gemini.google.com/_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate')) {
    let requestBody = undefined;
    const requestInit = args[1];

    if (requestInit && requestInit.body) {
      const clonedBody = requestInit.body.clone();
      clonedBody.text().then(bodyText => {
        requestBody = bodyText;
        const requestDetails = {
          url: url,
          method: requestInit.method || 'GET',
          requestBody: requestBody
        };
        window.postMessage({ type: 'GEMINI_FETCH_REQUEST', details: requestDetails }, '*');
      }).catch(e => console.error("Main World: Error cloning fetch request body:", e));
    } else {
      const requestDetails = {
        url: url,
        method: requestInit ? requestInit.method : 'GET',
        requestBody: requestBody
      };
      window.postMessage({ type: 'GEMINI_FETCH_REQUEST', details: requestDetails }, '*');
    }
  }

  return originalFetch.apply(this, arguments)
    .then(response => {
      if (url.includes('https://gemini.google.com/_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate')) {
        const clonedResponse = response.clone();
        clonedResponse.text().then(body => {
          const responseDetails = {
            url: url,
            status: response.status,
            statusText: response.statusText,
            headers: Array.from(response.headers.entries()),
            body: body
          };
          window.postMessage({ type: 'GEMINI_FETCH_RESPONSE', details: responseDetails }, '*');
        });
      }
      return response;
    })
    .catch(error => {
      console.error('Main World: Gemini fetch error:', error);
      throw error;
    });
};

// --- Intercept XMLHttpRequest ---
const originalXHRopen = XMLHttpRequest.prototype.open;
const originalXHRsend = XMLHttpRequest.prototype.send;

XMLHttpRequest.prototype.open = function(method, url) {
  this._method = method;
  this._url = url;
  originalXHRopen.apply(this, arguments);
};

XMLHttpRequest.prototype.send = function(body) {
  if (this._url.includes('https://gemini.google.com/_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate')) {
    let requestBody = body;

    // If body is FormData, extract f.req
    if (body instanceof FormData) {
      const fReq = body.get('f.req');
      if (fReq) {
        requestBody = fReq; // Send the string value of f.req
      } else {
        // If FormData exists but f.req is not found, stringify the FormData for inspection
        const formDataEntries = [];
        for (let pair of body.entries()) {
          formDataEntries.push(`${pair[0]}=${pair[1]}`);
        }
        requestBody = `FormData: ${formDataEntries.join('&')}`;
      }
    }

    const requestDetails = {
      url: this._url,
      method: this._method,
      requestBody: requestBody
    };
    window.postMessage({ type: 'GEMINI_XHR_REQUEST', details: requestDetails }, '*');

    this.addEventListener('load', function() {
      if (this._url.includes('https://gemini.google.com/_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate')) {
        const responseDetails = {
          url: this._url,
          status: this.status,
          statusText: this.statusText,
          responseBody: this.responseText // For XHR, responseText is usually available
        };
        window.postMessage({ type: 'GEMINI_XHR_RESPONSE', details: responseDetails }, '*');
      }
    });

    this.addEventListener('error', function() {
      console.error('Main World: XHR error for URL:', this._url);
    });

    this.addEventListener('abort', function() {
      console.warn('Main World: XHR aborted for URL:', this._url);
    });

  }

  originalXHRsend.apply(this, arguments);
};