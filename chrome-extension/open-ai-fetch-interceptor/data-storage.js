// This script handles saving and retrieving intercepted request data from chrome.storage.local.
// It assumes '''logger.js''' is loaded before it to provide the Logger object.

const logger = Logger.create();

const STORAGE_KEY = 'interceptedRequests';
const MAX_STORED_REQUESTS = 100; // 최대 100개의 메시지만 저장

// CAS-style retry wrapper for chrome.storage.local operations
class StorageRetryWrapper {
  constructor(maxRetries = 5, initialDelayMs = 10, maxDelayMs = 500) {
    this.maxRetries = maxRetries;
    this.initialDelayMs = initialDelayMs;
    this.maxDelayMs = maxDelayMs;
    this.logger = Logger.create();
  }

  // Exponential backoff delay calculation
  calculateDelay(attempt) {
    const delay = Math.min(
        this.initialDelayMs * Math.pow(2, attempt),
        this.maxDelayMs
    );
    // Add jitter to prevent thundering herd
    return delay + Math.random() * (delay * 0.1);
  }

  // Generic CAS-style operation with retry
  async performCASOperation(storageKey, modifyFunction) {
    for (let attempt = 0; attempt < this.maxRetries; attempt++) {
      try {
        // 1. Read current value
        const currentResult = await chrome.storage.local.get(storageKey);
        const currentValue = currentResult[storageKey] || [];

        // 2. Create a snapshot for comparison
        const currentSnapshot = JSON.stringify(currentValue);

        // 3. Apply modification function
        const modifiedValue = await modifyFunction(currentValue);

        // 4. Re-read to check for concurrent modifications
        const recheckResult = await chrome.storage.local.get(storageKey);
        const recheckValue = recheckResult[storageKey] || [];
        const recheckSnapshot = JSON.stringify(recheckValue);

        // 5. If values haven't changed, write the modified value
        if (currentSnapshot === recheckSnapshot) {
          await chrome.storage.local.set({[storageKey]: modifiedValue});
          this.logger.debug(
              `CAS operation succeeded on attempt ${attempt + 1}`);
          return modifiedValue;
        }

        // 6. Conflict detected, prepare for retry
        this.logger.debug(
            `CAS conflict detected on attempt ${attempt + 1}, retrying...`);

        if (attempt < this.maxRetries - 1) {
          const delay = this.calculateDelay(attempt);
          await new Promise(resolve => setTimeout(resolve, delay));
        }

      } catch (error) {
        this.logger.error(`CAS operation failed on attempt ${attempt + 1}:`,
            error);
        if (attempt === this.maxRetries - 1) {
          throw error;
        }

        const delay = this.calculateDelay(attempt);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }

    throw new Error(`CAS operation failed after ${this.maxRetries} attempts`);
  }
}

// Initialize the retry wrapper
const storageRetryWrapper = new StorageRetryWrapper();

// Updated saveInterceptedRequest function using CAS pattern
async function saveInterceptedRequest(requestData) {
  try {
    // Early validation
    if (!requestData || requestData.length === 0) {
      logger.debug("Received empty request data, skipping save.");
      return;
    }
    logger.info(requestData);

    // Use CAS wrapper for the critical section
    await storageRetryWrapper.performCASOperation(STORAGE_KEY,
        async (currentStoredRequests) => {
          // 1. Flatten existing data for backward compatibility
          const flatStoredMessages = currentStoredRequests.flat();

          // 2. Create set of existing message IDs for fast lookup
          const existingMessageIds = new Set(
              flatStoredMessages.map(msg => msg.id));

          // 3. Filter for truly new messages
          const newUniqueNodes = requestData.filter(node =>
              !node?.id || !existingMessageIds.has(node.id)
          );

          // 4. If no new messages, return current state unchanged
          if (newUniqueNodes.length === 0) {
            logger.debug(
                "All incoming messages were already in storage. Nothing to save.");
            return currentStoredRequests; // No change
          }

          // 5. Merge new messages at the front and trim to max size
          const updatedMessages = [...newUniqueNodes, ...flatStoredMessages];
          const trimmedMessages = updatedMessages.slice(0, MAX_STORED_REQUESTS);

          logger.info(
              `${newUniqueNodes.length} new unique messages saved. Total messages: ${trimmedMessages.length}`
          );

          return trimmedMessages;
        });

  } catch (error) {
    logger.error('Failed to save intercepted request after retries:', error);
    throw error;
  }
}

async function getSavedData(messageId) {
  const result = await chrome.storage.local.get(STORAGE_KEY);
  const allStoredData = result[STORAGE_KEY] || [];
  logger.debug(
      `Getting stored data for messageId: ${messageId}; total items: ${allStoredData.length}`);

  const foundItem = allStoredData.find(item => item.id === messageId);
  if (foundItem) {
    logger.debug(`${messageId}에 해당하는 데이터를 찾았습니다.`, foundItem);
    return foundItem;
  } else {
    logger.debug(`${messageId}에 해당하는 데이터가 없습니다.`);
  }
}