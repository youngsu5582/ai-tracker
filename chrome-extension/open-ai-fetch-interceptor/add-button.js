logger.info('add-button js 로드됨');

// 메시지 전체를 담고 있는 컨테이너 요소의 선택자
const MESSAGE_CONTAINER_SELECTOR =
    '[role="presentation"]';

// 우리가 찾고 있는 어시스턴트 메시지 요소의 선택자
const ASSISTANT_MESSAGE_SELECTOR =
    'article[data-turn-id]';

// 어시스턴스 메시지 - 하위 컨테이너 ( data-message-id 포함 ) - data-message-id 를 통해 매칭
const BUTTON_SELECTOR =
    '[data-testid="copy-turn-action-button"]';

const CUSTOM_BUTTON_CLASS = 'my-custom-server-button';

const INITIAL_DELAY_MS = 500;
const MAX_DELAY_MS = 2000;
// 보수적으로 5분
const TOTAL_DELAY_MS = 300 * 1000;

/**
 * 어시스턴트 메시지 블록에 커스텀 버튼을 추가합니다.
 * 지수 백오프 재시도 로직과 3분의 타임아웃이 포함되어 있습니다.
 * @param {HTMLElement} assistantMessageBlock - 버튼을 추가할 <article> 요소
 */
function addCustomButton(messageBlock) {
  logger.debug(
      `[addCustomButton] Called for messageBlock with data-turn-id: ${messageBlock.dataset.turnId}`);
  if (messageBlock.querySelector(`.${CUSTOM_BUTTON_CLASS}`)) {
    logger.debug('[addCustomButton] Custom button already exists. Returning.');
    return;
  }

  const startTime = Date.now();
  let currentDelay = INITIAL_DELAY_MS;

  // 실제 툴바 요소를 찾습니다.
  const tryToAddButton = () => {
    logger.debug(
        `[tryToAddButton] Attempting to add button. Current messageBlock.dataset.turnId: ${messageBlock.dataset.turnId}`);
    if (Date.now() - startTime > TOTAL_DELAY_MS) {
      logger.error('[tryToAddButton] 버튼 추가 시간 초과 (3분): 툴바를 찾을 수 없습니다.',
          messageBlock);
      return;
    }
    const referenceButton = messageBlock.querySelector(BUTTON_SELECTOR);
    if (referenceButton) {
      const toolbar = referenceButton.parentElement;

      if (toolbar) {
        const button = document.createElement('button');
        button.textContent = '나의 버튼';
        button.className = referenceButton.className + ' '
            + CUSTOM_BUTTON_CLASS; // Add custom class
        // 필요에 따라 스타일링을 위한 클래스
        button.onclick = () => {
          // 버튼 클릭 시점에 messageBlock 내에서 data-message-id를 가진 div를 다시 찾습니다.
          const messageIdDiv = messageBlock.querySelector(
              'div[data-message-id]');
          const finalTurnId = messageIdDiv ? messageIdDiv.dataset.messageId
              : null;

          logger.debug(
              `[onclick] Button clicked. finalTurnId (from messageIdDiv): ${finalTurnId}. messageBlock.dataset.turnId: ${messageBlock.dataset.turnId}`);

          if (finalTurnId && !finalTurnId.startsWith('request-')
              && !finalTurnId.startsWith('placeholder-request-')) {
            alert(`서버에 데이터 전송합니다! messageID:${finalTurnId}`);
            const message = {
              type: 'SERVER_SEND_BUTTON_CLICKED',
              payload: {
                turnId: finalTurnId
              }
            }
            chrome.runtime.sendMessage(message, (response) => {
              if (chrome.runtime.lastError) {
                logger.error(
                    `[onclick] 백그라운드 메시지 전송 실패:${JSON.stringify(
                        message)} | error: ${chrome.runtime.lastError.message}`);
              } else {
                logger.info(
                    `[onclick] 백그라운드 메시지 전송:${JSON.stringify(message)}`);
              }
            })
          } else {
            alert('메시지 ID가 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.');
            logger.warn(
                `[onclick] Attempted to send message before final turnId was available. finalTurnId: ${finalTurnId}. messageBlock.dataset.turnId: ${messageBlock.dataset.turnId}`);
          }
        }
        button.innerHTML = '<span class="flex items-center justify-center touch:w-10 h-8 w-8">✨</span>';

        toolbar.appendChild(button);
        logger.info('새로운 버튼이 툴바에 성공적으로 추가되었습니다.');
        return;
      }
    }

    setTimeout(tryToAddButton, currentDelay);
    currentDelay = Math.min(currentDelay * 2, MAX_DELAY_MS);
  }
  // 처음 시도
  tryToAddButton();
}

// 2. DOM 변경을 감시할 MutationObserver를 생성합니다.
const observer = new MutationObserver((mutationsList) => {
  // 페이지에 어떤 변경이든 일어나면 이 함수가 실행됩니다.
  for (const mutation of mutationsList) {
    // 추가된 노드(요소)가 있는지 확인합니다.
    if (mutation.addedNodes.length) {
      mutation.addedNodes.forEach(node => {
        // 추가된 노드가 HTML 요소인지 먼저 확인합니다.
        if (node.nodeType === Node.ELEMENT_NODE) {
          // 추가된 노드 자체가 우리가 찾던 메시지블록인지, 또는 그 내부에 메시지 블록을 포함하고 있는지
          // 확인하여 버튼을 추가합니다.
          if (node.matches(ASSISTANT_MESSAGE_SELECTOR)) {
            addCustomButton(node);
          }
          node.querySelectorAll(ASSISTANT_MESSAGE_SELECTOR)
          .forEach(addCustomButton);
        }
      });
    }
  }
});

// 3. 문서 전체(document.body)를 대상으로 감시를 시작합니다.
// 자식 요소(childList)와 그 아래 모든 자손 요소(subtree)의 변화를
// 감지하도록
// 설정합니다.

function startObserving(targetNode) {
  logger.info('감시 대상 노드를 찾았습니다. observer 를 시작합니다.');
  observer.observe(targetNode, {
    childList: true,
    subtree: true,
  });
}

let findTimeout = 0;

const findContainerInterval = setInterval(() => {
  const container = document.querySelector(MESSAGE_CONTAINER_SELECTOR);
  if (container) {
    clearInterval(findContainerInterval);
    clearTimeout(findTimeout);
    startObserving(container);
  }
}, 500);

findTimeout = setTimeout(() => {
  clearInterval(findContainerInterval);
  logger.error(`30초 내에 감시 대상 컨테이너(${MESSAGE_CONTAINER_SELECTOR})를 찾지 못했습니다.`);
}, 30 * 1000);

logger.info('MutationObserver가 DOM 변경 감시를 시작했습니다.');