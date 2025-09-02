// This background script listens for messages from content scripts.
// It depends on 'logger.js' and 'data-storage.js' being loaded first.

logger.info("백그라운드 서비스 워커 시작됨: Fetch 오버라이딩 데이터 수신 준비 완료");
// is my sample URL!
const DEFAULT_SERVER_URL = 'http://localhost:11240/api/data/capture';
const POST_CONVERSATION_PATH = '/f/conversation';

// Listen for messages from content scripts
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  logger.debug(`[Data] Received message:`, message);
  if (message.type === 'FETCH_INTERCEPTED') {
    const {pathname} = new URL(message.url);
    const isPostConversation = pathname.endsWith(POST_CONVERSATION_PATH);
    if (isPostConversation) {
      executePostMessageAPI(message);
    } else {
      executeGetMessageListAPI(message);
    }
  }
  if (message.type === 'SERVER_SEND_BUTTON_CLICKED') {
    executeServerSendButtonClick(message, sendResponse);
    return true;
  }
});

function executeGetMessageListAPI(message) {
  logger.debug("Processing GET message list API data:", message);
  const body = message.responseBody || {};
  const conversationId = body.conversation_id;
  const allNodes = body.mapping ? Object.values(body.mapping) : [];

  const filteredNodes = allNodes.filter(node => {
    if (!node.message || !node.message.content) {
      return false;
    }
    const {author, content} = node.message;
    const role = author?.role;
    const isCorrectAuthor = role === 'user' || role === 'assistant';
    const isTextContent = content.content_type === 'text';
    const hasContentInParts = Array.isArray(content.parts)
        && content.parts.some(
            (part) => typeof part === 'string' && part.trim() !== '');
    return isCorrectAuthor && isTextContent && hasContentInParts;
  });

  // Add conversation_id to each filtered node
  const nodes = filteredNodes.map(node => ({
    ...node,
    conversation_id: conversationId
  }));

  saveInterceptedRequest(nodes);
}

function executePostMessageAPI(message) {
  logger.debug("Processing POST message API data:", message);
  const conversationId = message.requestBody.conversation_id || '';
  const nodeBody = message.responseBody || {};
  const node = [{...nodeBody, conversationId}];
  saveInterceptedRequest(node);
}

async function executeServerSendButtonClick(message, sendResponse) {
  const {turnId} = message.payload;
  logger.debug(`서버로 보낼 데이터 준비, turnId: ${turnId}`);

  try {
    const {apiEndpoint} = await chrome.storage.sync.get({
      apiEndpoint: DEFAULT_SERVER_URL
    });

    try {
      new URL(apiEndpoint);
    } catch {
      throw new Error(`Invalid apiEndpoint: ${apiEndpoint}`);
    }

    // ✨ NEW: Request permission for the target endpoint
    const targetUrl = new URL(apiEndpoint);
    const targetOrigin = `${targetUrl.protocol}//${targetUrl.host}/*`;

    logger.debug(`Checking permission for: ${targetOrigin}`);

    const hasPermission = await chrome.permissions.contains({
      origins: [targetOrigin]
    });

    if (!hasPermission) {
      logger.info(`Requesting permission for: ${targetOrigin}`);
      const granted = await chrome.permissions.request({
        origins: [targetOrigin]
      });
      if (!granted) {
        const errorMessage = `Permission denied for ${targetOrigin}`;
        logger.error(errorMessage);
        sendResponse({
          status: 'error',
          message: errorMessage
        });
        return;
      }
      logger.info(`Permission granted for: ${targetOrigin}`);
    }

    const savedData = await getSavedData(turnId);

    if (!savedData) {
      throw new Error(`turnId '${turnId}'에 해당하는 데이터를 찾을 수 없습니다.`);
    }

    logger.info(`Sending data to configured endpoint: ${apiEndpoint}`);
    logger.info(
        `Data ID: ${savedData.id} | role: ${savedData.message.author.role}`)
    const ac = new AbortController();
    const timer = setTimeout(() => ac.abort(), 10 * 1000);
    const serverAPIResponse = await fetch(
        apiEndpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(savedData),
          signal: ac.signal,
        }).finally(() => clearTimeout(timer));

    if (!serverAPIResponse.ok) {
      // 서버가 에러 응답을 보냈을 경우, 응답 본문을 텍스트로 읽어서 에러 메시지에 포함
      const errorText = await serverAPIResponse.text();
      throw new Error(
          `서버 전송 실패: ${serverAPIResponse.status} ${serverAPIResponse.statusText} | ${errorText}`);
    }

    // 응답이 비어있을 수 있으므로, 먼저 텍스트로 읽어본다.
    const responseText = await serverAPIResponse.text();
    if (!responseText) {
      logger.info(`서버 전송 성공: 응답 본문이 비어있습니다.`);
      sendResponse(
          {
            status: 'ok',
            message: '서버 전송에 성공했지만, 응답 본문은 비어있습니다.',
            foundMessage: savedData
          });
      return;
    }

    const result = JSON.parse(responseText); // 텍스트를 JSON으로 파싱
    logger.info(`서버 전송 성공:`, result);

    sendResponse(
        {status: 'ok', message: '서버 전송에 성공했습니다.', foundMessage: savedData});

  } catch (error) {
    logger.error(`서버 전송 중 에러 발생:`, error);
    sendResponse({status: 'error', message: error.message});
  }
}
