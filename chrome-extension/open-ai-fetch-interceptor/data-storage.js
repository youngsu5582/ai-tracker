// This script handles saving and retrieving intercepted request data from chrome.storage.local.
// It assumes 'logger.js' is loaded before it to provide the Logger object.

const logger = Logger.create();

const STORAGE_KEY = 'interceptedRequests';
const MAX_STORED_REQUESTS = 100; // 최대 100개의 메시지만 저장

// 데이터를 chrome.storage.local에 단일 배열로 저장하는 함수
async function saveInterceptedRequest(requestData) {
  try {
    // 새로운 데이터(노드 배열)가 비어있으면 저장을 시도하지 않습니다.
    if (!requestData || requestData.length === 0) {
      logger.debug("Received empty request data, skipping save.");
      return;
    }

    // 현재 저장된 요청 목록을 가져옵니다.
    const result = await chrome.storage.local.get(STORAGE_KEY);
    const storedRequests = result[STORAGE_KEY] || [];

    // 1. 기존 데이터를 단일 메시지 배열로 펼칩니다. (하위 호환성)
    const flatStoredMessages = storedRequests.flat();

    // 2. 기존 모든 메시지의 ID를 Set으로 만들어 빠른 조회를 준비합니다.
    const existingMessageIds = new Set(flatStoredMessages.map(msg => msg.id));

    // 3. 새로 들어온 노드 중에서, 메시지 ID가 기존 Set에 없는 것만 필터링합니다.
    const newUniqueNodes = requestData.filter(node =>
        !node?.id || !existingMessageIds.has(node.id)
    );

    // 4. 저장할 새로운 유니크 메시지가 없다면 함수를 종료합니다.
    if (newUniqueNodes.length === 0) {
      logger.debug(
          "All incoming messages were already in storage. Nothing to save.");
      return;
    }

    // 5. 새로운 메시지들을 펼쳐서 기존 메시지 목록 앞에 추가합니다.
    const updatedMessages = [...newUniqueNodes, ...flatStoredMessages];

    // 6. 최대 저장 개수를 초과하면 오래된 메시지를 제거합니다.
    const trimmedMessages = updatedMessages.slice(0, MAX_STORED_REQUESTS);

    // 7. 업데이트된 단일 배열을 저장합니다.
    await chrome.storage.local.set({[STORAGE_KEY]: trimmedMessages});
    logger.info(
        `${newUniqueNodes.length} new unique messages saved. Total messages:`,
        trimmedMessages.length);

  } catch (error) {
    logger.error("Error saving request to storage:", error);
  }
}

async function getSavedData(messageId) {
  const result = await chrome.storage.local.get(STORAGE_KEY);
  const allStoredData = result[STORAGE_KEY] || [];
  logger.debug(`Getting all stored data for messageId: ${messageId}`, allStoredData);
  
  const foundItem = allStoredData.find(item => item.id === messageId);
  if (foundItem) {
    logger.debug(`${messageId}에 해당하는 데이터를 찾았습니다.`, foundItem);
    return foundItem;
  } else {
    logger.debug(`${messageId}에 해당하는 데이터가 없습니다.`);
  }
}
