# OpenAI Fetch Interceptor - Chrome Extension

---

## English

### 1. Overview

This Chrome extension is specifically designed to intercept and capture the full request and response data from conversations on OpenAI's ChatGPT website. It reliably captures entire conversation turns, including streaming content, and provides a mechanism to send this comprehensive data to a user-configured backend server.

### 2. Features

- **Comprehensive Capture**: Intercepts `fetch` requests to capture entire conversation turns, not just the final text.
- **Streaming Support**: Correctly handles and reconstructs complete messages from `text/event-stream` responses.
- **UI Integration**: Adds a custom button ('✨') to the ChatGPT interface for manual and explicit data forwarding.
- **Configurable Endpoint**: Allows users to easily specify their own backend server URL via the extension's options page.
- **Local Caching**: Temporarily stores captured data in the browser's local storage before sending.

### 3. How it Works (Data Flow)

The extension operates with the following flow, all within the `open-ai-fetch-interceptor` directory:

1.  **Injection**: `inject.js` runs first, injecting `fetch_interceptor.js` into the web page's main context.
2.  **Interception**: `fetch_interceptor.js` overrides the `fetch` function to watch for API calls to ChatGPT.
3.  **Message Passing**: When a response is captured, it's passed from `fetch_interceptor.js` → `content.js` → `background.js`.
4.  **Data Caching**: `background.js` receives the data and uses `data-storage.js` to save the conversation turn into `chrome.storage.local`.
5.  **UI Interaction**: `add-button.js` observes the page and injects a '✨' button onto new assistant messages.
6.  **Sending Data**: When the user clicks the '✨' button, `add-button.js` signals `background.js`, which then retrieves the data from storage and sends it to the configured backend server.

### 4. Setup and Configuration

1.  **Load the Extension**:
    - Open Chrome and navigate to `chrome://extensions`.
    - Enable "Developer mode".
    - Click "Load unpacked" and select the `chrome-extension/open-ai-fetch-interceptor` directory.
2.  **Configure Endpoint**:
    - Right-click the extension's icon in the Chrome toolbar and select "Options".
    - In the options page, enter the full URL for your backend API endpoint (e.g., `http://localhost:11240/api/data/capture`).
    - Click "Save".

### 5. Example Data

The data sent to your server will be in the following JSON format. The `parts` array contains the full, reconstructed response from the AI.

```json
{
  "id": "94cf5527-1f87-471f-89f4-c19586951327",
  "parent": "e76a6ec0-e500-42d8-aacc-6ca96789383a",
  "conservation_id": "68b1719b-659c-8326-9dbb-5cabef0fc52f",
  "message": {
    "id": "94cf5527-1f87-471f-89f4-c19586951327",
    "author": {
      "role": "assistant"
    },
    "create_time": 1756537581.914188,
    "content": {
      "content_type": "text",
      "parts": [
        "<The full text content of the assistant's response goes here...>"
      ]
    },
    "status": "finished_successfully",
    "end_turn": true,
    "metadata": {
      "model_slug": "gpt-5-thinking",
      "parent_id": "e76a6ec0-e500-42d8-aacc-6ca96789383a"
    }
  }
}
```

---
<br>

## 한글

### 1. 개요

이 크롬 확장 프로그램은 OpenAI의 ChatGPT 웹사이트에서 이루어지는 대화의 전체 요청 및 응답 데이터를 가로채고 캡처하기 위해 특별히 설계되었습니다. 스트리밍 콘텐츠를 포함한 전체 대화 턴을 안정적으로 캡처하며, 이 포괄적인 데이터를 사용자가 설정한 백엔드 서버로 전송하는 메커니즘을 제공합니다.

### 2. 주요 기능

- **포괄적인 데이터 캡처**: 최종 텍스트뿐만 아니라, 전체 대화의 맥락을 파악하기 위해 `fetch` 요청 자체를 가로챕니다.
- **스트리밍 응답 지원**: `text/event-stream`으로 전송되는 응답을 올바르게 처리하고 완전한 메시지로 재구성합니다.
- **UI 연동**: ChatGPT 인터페이스에 커스텀 버튼('✨')을 추가하여, 사용자가 명시적으로 데이터를 전송할 수 있도록 합니다.
- **설정 가능한 엔드포인트**: 확장 프로그램의 옵션 페이지를 통해 사용자가 직접 자신의 백엔드 서버 URL을 쉽게 지정할 수 있습니다.
- **로컬 캐싱**: 캡처된 데이터를 전송하기 전에 브라우저의 로컬 스토리지에 임시 저장합니다.

### 3. 동작 방식 (데이터 흐름)

확장 프로그램은 `open-ai-fetch-interceptor` 디렉터리 내에서 아래와 같은 순서로 동작합니다.

1.  **스크립트 주입**: `inject.js`가 가장 먼저 실행되어, 웹 페이지의 메인 컨텍스트에 `fetch_interceptor.js`를 주입합니다.
2.  **요청 가로채기**: `fetch_interceptor.js`가 `fetch` 함수를 오버라이드하여 ChatGPT로 가는 API 호출을 감시합니다.
3.  **메시지 전달**: 응답이 캡처되면, `fetch_interceptor.js` → `content.js` → `background.js` 순서로 데이터가 전달됩니다.
4.  **데이터 캐싱**: `background.js`는 전달받은 데이터를 `data-storage.js`를 통해 `chrome.storage.local`에 저장합니다.
5.  **UI 상호작용**: `add-button.js`는 페이지의 변화를 감지하여 새로운 어시스턴트 메시지에 '✨' 버튼을 삽입합니다.
6.  **데이터 전송**: 사용자가 '✨' 버튼을 클릭하면 `add-button.js`가 `background.js`에 신호를 보내고, `background.js`는 스토리지에서 해당 데이터를 가져와 설정된 백엔드 서버로 전송합니다.

### 4. 설치 및 설정

1.  **확장 프로그램 로드**:
    - 크롬 브라우저에서 `chrome://extensions` 주소로 이동합니다.
    - "개발자 모드"를 활성화합니다.
    - "압축 해제된 확장 프로그램을 로드합니다"를 클릭하고 `chrome-extension/open-ai-fetch-interceptor` 디렉터리를 선택합니다.
2.  **엔드포인트 설정**:
    - 크롬 툴바의 확장 프로그램 아이콘을 우클릭한 후 "옵션"을 선택합니다.
    - 옵션 페이지에서 백엔드 API 엔드포인트의 전체 URL을 입력합니다. (예: `http://localhost:11240/api/data/capture`)
    - "Save" 버튼을 클릭합니다.

### 5. 데이터 예시

서버로 전송되는 데이터는 아래와 같은 JSON 형식입니다. `parts` 배열에 AI의 전체 응답 내용이 재구성되어 포함됩니다.

```json
{
  "id": "94cf5527-1f87-471f-89f4-c19586951327",
  "parent": "e76a6ec0-e500-42d8-aacc-6ca96789383a",
  "conservation_id": "68b1719b-659c-8326-9dbb-5cabef0fc52f",
  "message": {
    "id": "94cf5527-1f87-471f-89f4-c19586951327",
    "author": { "role": "assistant" },
    "create_time": 1756537581.914188,
    "content": {
      "content_type": "text",
      "parts": [
        "<AI 어시스턴트의 전체 응답 텍스트가 여기에 들어갑니다...>"
      ]
    },
    "status": "finished_successfully",
    "end_turn": true,
    "metadata": {
      "model_slug": "gpt-5-thinking",
      "parent_id": "e76a6ec0-e500-42d8-aacc-6ca96789383a"
    }
  }
}
```