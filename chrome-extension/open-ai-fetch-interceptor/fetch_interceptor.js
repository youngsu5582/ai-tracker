// This script runs in the main world of the webpage to intercept fetch requests.
(function () {
  const originalFetch = window.fetch;
  const LOGGER = {
    debugEnabled: false, // Set to true for debugging, false for production
    debug: function (...args) {
      if (this.debugEnabled) {
        console.log("[MainWorld Debug]", ...args);
      }
    },
    warn: function (...args) {
      console.log("[MainWorld Warn]", ...args);
    },
    info: function (...args) {
      console.log("[MainWorld Info]", ...args);
    },
    error: function (...args) {
      console.error("[MainWorld Error]", ...args);
    }
  };

  LOGGER.info("Loaded: Intercepting window.fetch");
  const API_ENDPOINTS = [
    {
      method: "GET",
      url: /^https:\/\/chatgpt\.com\/backend-api\/conversation\/[a-f0-9-]+$/,
    },
    {
      method: "POST",
      url: "https://chatgpt.com/backend-api/f/conversation",
    }
  ]

  function isTargetEndpoints(request) {
    return API_ENDPOINTS.some(endpoint => {
      const methodMatch =
          (request.method || 'GET').toUpperCase() === endpoint.method;
      let urlMatch = false;
      if (endpoint.url instanceof RegExp) {
        // endpoint.url이 정규식이면 test 메소드로 확인
        urlMatch = endpoint.url.test(request.url);
      } else if (typeof endpoint.url === 'string') {
        // endpoint.url이 문자열이면 기존 로직으로 확인
        urlMatch = endpoint.url.endsWith('*') ?
            request.url.startsWith(endpoint.url.slice(0, -1)) :
            request.url === endpoint.url;
      }
      return methodMatch && urlMatch;
    })
  }

  window.fetch = async (...args) => {
    const url = typeof args[0] === 'string' ? args[0] : args[0].url;
    const options = args[1] || {};

    const method = options.method || 'GET';

    // init 메소드는 잡을 필요 없으므로 제거
    if (!isTargetEndpoints({url, method}) || url.endsWith('init')) {
      LOGGER.debug(`[MainWorld Interceptor] url, method does not contain endpoints\n 
          url:${url}, method: ${method}`)
      return await originalFetch.apply(this, args);
    }
    LOGGER.info(`✅ intercept request. url:${url}, method:${method}`);
    // Perform the original fetch request
    const response = await originalFetch.apply(this, args);

    const contentType = response.headers.get('content-type');

    // 스트리밍 응답 처리
    if (contentType && contentType.includes('text/event-stream')) {
      LOGGER.info(
          `🚀 [Event Stream Detected] Processing SSE response. url: ${url}`);

      const [streamForPage, streamForInterceptor] = response.body.tee();
      const responseForPage = new Response(
          streamForPage, {
            headers: response.headers,
            status: response.status,
            statusText: response.statusText,
          }
      );
      (async () => {
        const reader = streamForInterceptor.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = "";
        let fullResponseText = "";
        let jsonObject = {};
        let streamFinishedByDoneMessage = false;

        const processChunk = (chunkValue) => {
          buffer += decoder.decode(chunkValue, {stream: true});
          let parts = buffer.split('\n');
          buffer = parts.pop(); // Keep the last, possibly incomplete, part

          for (const part of parts) {
            if (part.startsWith('data:')) {
              const data = part.substring(5).trim();
              if (data === '[DONE]') {
                LOGGER.info("✅ [DONE] received. Stream processing finished.");
                streamFinishedByDoneMessage = true;
                return; // Stop processing parts in this chunk
              }
              try {
                const jsonPart = JSON.parse(data);
                // Handle initial message object (o === 'add')
                if (jsonPart.o === 'add' && jsonPart.v && jsonPart.v.message) {
                  jsonObject.message = jsonPart.v.message;
                  jsonObject.parent = jsonPart.v.message.metadata?.parent_id;
                  jsonObject.id = jsonPart.v.message.id;
                  jsonObject.children = [];
                }

                // Handle content accumulation
                if (typeof jsonPart.v === 'string') {
                  fullResponseText += jsonPart.v;
                }
                else if (jsonPart.p && jsonPart.p.startsWith('/message/content/parts/') && typeof jsonPart.v === 'string') {
                  fullResponseText += jsonPart.v;
                }
                else if (Array.isArray(jsonPart.v)) {
                  for (const patch of jsonPart.v) {
                    if (patch.p && patch.p.startsWith('/message/content/parts/') && patch.o === 'append' && typeof patch.v === 'string') {
                      fullResponseText += patch.v;
                    }
                  }
                }

              } catch (e) {
                LOGGER.warn(`⚠️ [WARN] Stream processing error. ${e.message}`);
              }
            }
          }
        };

        while (true) {
          const {value, done} = await reader.read();

          if (value) { // Process the chunk if it exists
            processChunk(value);
          }

          if (done || streamFinishedByDoneMessage) {
            LOGGER.info("✅ Stream reading complete or [DONE] message received.");
            break;
          }
        }

        // Final flush of the decoder and process any remaining buffer content
        // This ensures any multi-byte characters or final data not ending with \n are processed.
        const finalBufferContent = decoder.decode();
        if (finalBufferContent) {
          buffer += finalBufferContent; // Add to buffer for final processing
          let parts = buffer.split('\n');
          buffer = parts.pop(); // Should be empty or just a final [DONE]
          for (const part of parts) {
            if (part.startsWith('data:')) {
              const data = part.substring(5).trim();
              if (data && data !== '[DONE]') { // Avoid processing [DONE] again
                try {
                  const jsonPart = JSON.parse(data);
                  // Accumulate content from final part
                  if (typeof jsonPart.v === 'string') {
                    fullResponseText += jsonPart.v;
                  }
                  else if (jsonPart.p && jsonPart.p.startsWith('/message/content/parts/') && typeof jsonPart.v === 'string') {
                    fullResponseText += jsonPart.v;
                  }
                  else if (Array.isArray(jsonPart.v)) {
                    for (const patch of jsonPart.v) {
                      if (patch.p && patch.p.startsWith('/message/content/parts/') && patch.o === 'append' && typeof patch.v === 'string') {
                        fullResponseText += patch.v;
                      }
                    }
                  }
                } catch (e) {
                  LOGGER.warn(`⚠️ [WARN] Error processing final decoder flush part. ${e.message}`);
                }
              }
            }
          }
        }

        console.log(jsonObject);
        // Assign the fully concatenated text to the final object
        if (jsonObject.message && jsonObject.message.content) {
          // Ensure that .parts is an array before trying to access an index.
          if (!Array.isArray(jsonObject.message.content.parts)) {
            jsonObject.message.content.parts = [];
          }
          jsonObject.message.content.parts[0] = fullResponseText;
        } else {
          LOGGER.error("Cannot assemble final message, jsonObject is missing structure.", {jsonObject});
        }
        LOGGER.debug(`[Data]`, jsonObject);
        if (fullResponseText) {
          LOGGER.info(
              "➡️ [Sending to Content Script] Final conversation data prepared.");
          window.postMessage({
            type: 'FETCH_INTERCEPTED',
            url: url,
            method: method,
            requestBody: parseRequestBody(options.body),
            responseBody: jsonObject,
            responseHeaders: Object.fromEntries(response.headers.entries()),
            statusCode: response.status,
            timestamp: new Date().toISOString()
          }, '*');
        }
      })();
      return responseForPage;
    } else {
      LOGGER.info("[Regular Response Detected] Processing as JSON/text.");
      const clonedResponse = response.clone();
      const responseBody = await clonedResponse.json();
      window.postMessage({
        type: 'FETCH_INTERCEPTED',
        url: url,
        method: method,
        requestBody: parseRequestBody(options.body),
        responseBody: responseBody,
        responseHeaders: Object.fromEntries(response.headers.entries()),
        statusCode: response.status,
        timestamp: new Date().toISOString()
      }, '*');
    }
    return response;
  };

  const parseRequestBody = (body) => {
    if (!body) {
      return null;
    }
    if (typeof body === 'string') {
      try {
        // JSON 문자열이면 객체로 변환
        return JSON.parse(body);
      } catch (e) {
        // JSON이 아니면 원본 문자열 그대로 반환
        return body;
      }
    }
    // 문자열이 아닌 다른 타입이면 일단 그대로 반환
    return body;
  };
})();