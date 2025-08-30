// This background script listens for messages from content scripts.
// It depends on 'logger.js' and 'data-storage.js' being loaded first.

logger.info("백그라운드 서비스 워커 시작됨: Fetch 오버라이딩 데이터 수신 준비 완료");

// Listen for messages from content scripts
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  logger.debug(`[Data] Received message:`, message);
  if (message.type === 'FETCH_INTERCEPTED') {
    if (message.url.endsWith("f/conversation")) {
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

  const conversationId = message.responseBody.conversation_id;
  const allNodes = Object.values(message.responseBody.mapping);

  const filteredNodes = allNodes.filter(node => {
    if (!node.message || !node.message.content) {
      return false;
    }
    const {author, content} = node.message;
    const isCorrectAuthor = ['user', 'assistant'].includes(author.role);
    const isTextContent = content.content_type === 'text';
    const hasContentInParts = Array.isArray(content.parts)
        && content.parts.some(
            part => typeof part === 'string' && part.trim() !== '');
    return isCorrectAuthor && isTextContent && hasContentInParts;
  });

  // Add conversation_id to each filtered node
  const nodesWithConversationId = filteredNodes.map(node => ({
    ...node,
    conversation_id: conversationId
  }));

  saveInterceptedRequest(nodesWithConversationId);
}

function executePostMessageAPI(message) {
  logger.debug("Processing POST message API data:", message);
  const node = [message.responseBody];
  saveInterceptedRequest(node);
}

async function executeServerSendButtonClick(message, sendResponse) {
  const turnId = message.payload.turnId;
  logger.info(`서버로 보낼 데이터 준비, turnId: ${turnId}`);

  try {
    const {apiEndpoint} = await chrome.storage.sync.get({
      apiEndpoint: 'http://localhost:11240/api/data/capture'
    });

    const dataToSend = await getSavedData(turnId);

    if (!dataToSend) {
      throw new Error(`turnId '${turnId}'에 해당하는 데이터를 찾을 수 없습니다.`);
    }

    logger.info(`Sending data to configured endpoint: ${apiEndpoint}`);
    const serverAPIResponse = await fetch(
        apiEndpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(dataToSend),
        });

    if (!serverAPIResponse.ok) {
      throw new Error(
          `서버 전송 실패: ${serverAPIResponse.status} ${serverAPIResponse.statusText}`);
    }

    const result = await serverAPIResponse.json();
    logger.info(`서버 전송 성공:`, result);

    sendResponse(
        {status: 'ok', message: '서버 전송에 성공했습니다.', foundMessage: dataToSend});

  } catch (error) {
    logger.error(`서버 전송 중 에러 발생:`, error);
    sendResponse({status: 'error', message: error.message});
  }
}
