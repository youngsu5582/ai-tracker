console.log('Gemini Fetch Interceptor: Background service worker (version 0.1.7) loaded.');

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    // Only process messages related to StreamGenerate URL
    if (!request.details || (!request.details.url.includes('StreamGenerate') && !request.details.url.includes('batchexecute')) ) {
      return; // Skip if not StreamGenerate or batchexecute URL
    }

    if (request.type === "GEMINI_FETCH_REQUEST" || request.type === "GEMINI_XHR_REQUEST") {
      console.log('Background: Intercepted Gemini Request.', request.details.url);
      let rawBody = request.details.requestBody;

      if (rawBody) {
        // f.req is a string like 'f.req=%5Bnull%2C%22%5B%5B%5C%22...' or just the decoded JSON string
        let decodedBody = rawBody;
        if (typeof rawBody === 'string' && rawBody.startsWith('f.req=')) {
          try {
            decodedBody = decodeURIComponent(rawBody.substring(6)); // Remove 'f.req=' and decode
          } catch (e) {
            console.error('Background: Error decoding f.req:', e, rawBody);
            return;
          }
        }

        try {
          const parsedReq = JSON.parse(decodedBody);
          console.log('Background: Successfully parsed request body.', parsedReq);

          // Extract prompt from f.req structure
          let prompt = null;
          if (Array.isArray(parsedReq) && parsedReq.length > 0) {
            if (Array.isArray(parsedReq[0]) && parsedReq[0].length > 0) {
              if (Array.isArray(parsedReq[0][0]) && parsedReq[0][0].length > 0) {
                prompt = parsedReq[0][0][0];
              } else if (typeof parsedReq[0][0] === 'string') {
                prompt = parsedReq[0][0];
              }
            }
          }

          if (prompt) {
            console.log('Background: Extracted Prompt:', prompt);
          } else {
            console.warn('Background: Could not extract prompt from request body.', parsedReq);
          }
        } catch (e) {
          console.error('Background: Error parsing request body JSON:', e, decodedBody);
        }
      } else {
        console.log('Background: Request body is empty.');
      }
    } else if (request.type === "GEMINI_FETCH_RESPONSE" || request.type === "GEMINI_XHR_RESPONSE") {
      console.log('Background: Intercepted Gemini Response.', request.details.url);
      const rawResponse = request.details.body || request.details.responseBody;

      if (rawResponse) {
        try {
          // Gemini responses are often in a specific format: length\nJSON_chunk
          // We need to parse each chunk
          const chunks = rawResponse.split(/\d+\n/).filter(chunk => chunk.trim() !== '');
          let fullResponseText = '';

          chunks.forEach(chunk => {
            try {
              const parsedChunk = JSON.parse(chunk);
              // Assuming the actual response text is deeply nested, similar to the prompt
              // This might need adjustment based on actual response structure
              if (Array.isArray(parsedChunk) && parsedChunk.length > 0) {
                if (Array.isArray(parsedChunk[0]) && parsedChunk[0].length > 0) {
                  if (Array.isArray(parsedChunk[0][0]) && parsedChunk[0][0].length > 0) {
                    // This is a common path for actual text content
                    if (Array.isArray(parsedChunk[0][0][0]) && parsedChunk[0][0][0].length > 0) {
                      fullResponseText += parsedChunk[0][0][0][0];
                    } else if (typeof parsedChunk[0][0][0] === 'string') {
                      fullResponseText += parsedChunk[0][0][0];
                    }
                  }
                }
              }
            } catch (e) {
              // console.warn('Background: Could not parse response chunk JSON:', e, chunk);
            }
          });

          if (fullResponseText) {
            console.log('Background: Extracted Response:', fullResponseText);
          } else {
            console.warn('Background: Could not extract full response text.', rawResponse.substring(0, 200) + '...');
          }

        } catch (e) {
          console.error('Background: Error processing raw response:', e, rawResponse.substring(0, 200) + '...');
        }
      } else {
        console.log('Background: Response body is empty.');
      }
    }
  }
);