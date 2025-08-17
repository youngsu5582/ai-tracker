// chrome-extension/src/main_world_ai_tracker.js
// This script runs in the main world of the webpage to intercept fetch requests.

(function() {
    const originalFetch = window.fetch;

    // Logger for main_world_ai_tracker.js
    const MainWorldLogger = {
        debugEnabled: true, // Set to true for debugging, false for production
        debug: function(...args) {
            if (this.debugEnabled) {
                console.log("[MainWorld Debug]", ...args);
            }
        },
        info: function(...args) {
            console.log("[MainWorld Info]", ...args);
        },
        error: function(...args) {
            console.error("[MainWorld Error]", ...args);
        }
    };

    MainWorldLogger.info("MainWorld AI Tracker loaded: Intercepting window.fetch");

    window.fetch = async (...args) => {
        // Handle both string URL and Request object as the first argument
        const url = typeof args[0] === 'string' ? args[0] : args[0].url;
        const options = args[1] || {};

        // Identify AI API requests based on URL patterns
        // Focus specifically on ChatGPT's conversation API as requested
        const isChatGPTConversationApi = url.includes('chatgpt.com/backend-api/f/conversation') && !url.includes('/prepare');

        // Perform the original fetch request first
        const response = await originalFetch.apply(this, args);

        if (isChatGPTConversationApi) {
            MainWorldLogger.info(`[ChatGPT Conversation API Detected] Processing request for URL: ${url}`);

            let requestBody = null;
            let conversationIdFromRequest = null;
            let promptTextFromRequest = null;

            if (options.body) {
                try {
                    requestBody = JSON.parse(options.body);
                    MainWorldLogger.info("[Request Body Parsed] Successfully parsed request body.");
                    MainWorldLogger.debug("Full request body:", requestBody);

                    

                    // Extract conversationId from request body (specific to ChatGPT)
                    if (requestBody.conversation_id) {
                        conversationIdFromRequest = requestBody.conversation_id;
                        MainWorldLogger.info(`[Conversation ID] Extracted from request: ${conversationIdFromRequest}`);
                    }

                    // Extract prompt text from request body (specific to ChatGPT)
                    if (requestBody.messages && Array.isArray(requestBody.messages)) {
                        const userMessage = requestBody.messages.find(msg => msg.author.role === 'user');
                        if (userMessage && userMessage.content && userMessage.content.parts && Array.isArray(userMessage.content.parts)) {
                            promptTextFromRequest = userMessage.content.parts.join(' ');
                            MainWorldLogger.info(`[Prompt Extracted] ChatGPT style: "${promptTextFromRequest.substring(0, Math.min(promptTextFromRequest.length, 100))}..."`);
                        }
                    }

                } catch (e) {
                    MainWorldLogger.error("Failed to parse request body. It might not be JSON or malformed:", e);
                }
            }

            // Use tee() to create two independent streams from the response body
            const [ourStream, originalStream] = response.body.tee();

            // Create a new Response object with the original stream for the webpage
            const newResponse = new Response(originalStream, { headers: response.headers, status: response.status, statusText: response.statusText });

            // Process our stream asynchronously
            (async () => {
                let fullResponseText = '';
                let fullResponseBodyRaw = '';
                const decoder = new TextDecoder("utf-8");
                let buffer = "";
                let streamDoneLogical = false; // Flag to indicate logical stream end (e.g., [DONE] received)

                // Check if it's an Event Stream (text/event-stream)
                const contentType = response.headers.get('content-type'); // Use original response headers
                if (contentType && contentType.includes('text/event-stream')) {
                    MainWorldLogger.info("🚀 [Event Stream Detected] Reading response as stream.");
                    const reader = ourStream.getReader(); // Use our stream

                    while (true) {
                        const { value, done: readerDone } = await reader.read(); // Rename done to readerDone

                        if (readerDone) {
                            MainWorldLogger.info("✅ SSE Stream reader is done.");
                        }

                        // Decode current chunk and add to buffer
                        buffer += decoder.decode(value, { stream: true });

                        // Process complete lines from the buffer
                        let newlineIndex;
                        while ((newlineIndex = buffer.indexOf('\n')) !== -1) {
                            const line = buffer.substring(0, newlineIndex);
                            buffer = buffer.substring(newlineIndex + 1);

                            if (line.startsWith('data:')) {
                                const data = line.substring(5).trim();
                                fullResponseBodyRaw += line + '\n'; // Accumulate raw data lines

                                if (data === '[DONE]') {
                                    MainWorldLogger.info("✅ [DONE] received -> Terminating response processing.");
                                    streamDoneLogical = true; // Set flag for logical end
                                } else { // Only process if not [DONE]
                                    try {
                                        const jsonPart = JSON.parse(data);

                                        // --- ChatGPT specific delta parsing ---
                                        if (typeof jsonPart.v === 'string') {
                                            fullResponseText += jsonPart.v;
                                        } else if (jsonPart.p && jsonPart.p.startsWith('/message/content/parts/') && typeof jsonPart.v === 'string') {
                                            fullResponseText += jsonPart.v;
                                        }
                                        // --- End ChatGPT specific delta parsing ---

                                    } catch (e) {
                                        // Ignore non-JSON data lines or parsing errors within the stream
                                        // MainWorldLogger.debug("Non-JSON data line or parsing error in stream:", line, e);
                                    }
                                }
                            }
                        }

                        // Break condition: if reader is done AND buffer is empty
                        // This ensures all data from the stream has been processed
                        if (readerDone && buffer.length === 0) {
                            break;
                        }
                    }

                    // Final flush of the decoder to get any remaining characters not followed by a newline
                    fullResponseText += decoder.decode(new Uint8Array(), { stream: false });

                    MainWorldLogger.info(`✨ [Full Response Text Accumulated] Length: ${fullResponseText.length}. Content: "${fullResponseText.substring(0, Math.min(fullResponseText.length, 100))}..."`);

                } else {
                    // Not an Event Stream, try to parse as regular JSON or text
                    try {
                        const text = await ourStream.text(); // Use our stream
                        fullResponseBodyRaw = text; // Store raw text for fullResponseBodyRaw
                        MainWorldLogger.info("[Regular JSON Response] Successfully parsed response body.");
                        MainWorldLogger.debug("Full response body:", text);
                        responseText = text; // For non-streamed, responseText is the full text

                    } catch (err) {
                        MainWorldLogger.error("Failed to read or parse response body as JSON. It might be plain text or malformed:", err);
                        // Fallback to text if JSON parsing fails
                        try {
                            responseText = await ourStream.text();
                            MainWorldLogger.info(`[Response Extracted] As plain text: "${responseText.substring(0, Math.min(responseText.length, 100))}..."`);
                        } catch (textError) {
                            MainWorldLogger.error("Failed to read response body as plain text:", textError);
                        }
                    }
                }

                // Add a small intentional delay as requested, after all processing
                await new Promise(resolve => setTimeout(resolve, 50)); // 50ms delay

                // Send data to content script
                const payload = {
                    type: 'AI_API_CALL',
                    url: url,
                    requestBody: requestBody, // Send full request body for debugging/future use
                    responseBody: fullResponseBodyRaw, // Send full response body (raw stream data or JSON string) for debugging/future use
                    promptText: promptTextFromRequest, // Extracted prompt
                    responseText: fullResponseText, // Extracted response
                    conversationId: conversationIdFromRequest, // Extracted conversation ID
                    timestamp: new Date().toISOString(),
                    language: (promptTextFromRequest && promptTextFromRequest.trim().length > 0 && /[가-힣]/.test(promptTextFromRequest)) ? 'KO' : 'EN' // Set language based on prompt text
                };
                MainWorldLogger.info("➡️ [Sending to Content Script] Payload prepared and sending.");
                MainWorldLogger.debug("Payload details:", payload);
                window.postMessage(payload, '*'); // Consider restricting targetOrigin for security

            })();

            return newResponse; // Return the new response with the original stream to the webpage

        } else {
            // For non-target API calls, just return the original response without interception
            return response;
        }
    };
})();