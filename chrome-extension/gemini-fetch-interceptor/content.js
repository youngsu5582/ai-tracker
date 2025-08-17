console.log('Gemini Fetch Interceptor: Content script (version 0.1.7) loaded.');

// Inject main_world_script.js into the page's main world
const script = document.createElement('script');
script.src = chrome.runtime.getURL('main_world_script.js');
(document.head || document.documentElement).appendChild(script);

// Listen for messages from the main world script
window.addEventListener('message', function(event) {
  // Only accept messages from our main world script
  if (event.source === window && event.data.type && (
    event.data.type === 'GEMINI_FETCH_REQUEST' || 
    event.data.type === 'GEMINI_FETCH_RESPONSE' ||
    event.data.type === 'GEMINI_XHR_REQUEST' ||
    event.data.type === 'GEMINI_XHR_RESPONSE'
  )) {
    // Send the message to the background script
    chrome.runtime.sendMessage(event.data);
  }
});