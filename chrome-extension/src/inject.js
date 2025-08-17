// chrome-extension/src/inject.js
// This script is injected into the web page context to inject main_world_ai_tracker.js.

(function() {
    console.log("[Inject Info] Inject.js loaded: Injecting main_world_ai_tracker.js...");

    const script = document.createElement('script');
    script.src = chrome.runtime.getURL('src/main_world_ai_tracker.js');
    script.onload = function() {
        console.log("[Inject Info] main_world_ai_tracker.js injected and loaded.");
        this.remove(); // Clean up the script tag after it's loaded
    };
    (document.head || document.documentElement).appendChild(script);
})();
