# AI 활동 추적기

> 해당 코드는 gemini-cli 와 바이브코딩으로 작성되었습니다.

## 1. 프로젝트 개요

AI 활동 추적기는 ChatGPT 및 Gemini와 같은 AI 서비스 사용량을 추적, 분석하고 통계를 제공하도록 설계된 다중 모듈 애플리케이션입니다. 프롬프트 데이터를 캡처하는 Chrome 확장 프로그램, 데이터 처리 및 저장을 위한 Spring Boot 백엔드, 데이터 시각화를 위한 React 기반 프론트엔드로 구성됩니다.

## 2. 기능

*   **Chrome 확장 프로그램**:
    *   AI 웹 인터페이스(현재 ChatGPT 및 Gemini)에서 사용자 프롬프트, 모델 이름 및 타임스탬프를 캡처합니다.
    *   캡처된 데이터를 백엔드 서버로 전송합니다.
    *   쉬운 확장성을 위해 AI 플랫폼의 동적 감지를 지원합니다.
    *   다양한 입력 방식(타이핑, 복사-붙여넣기)에 대한 강력한 이벤트 처리를 포함합니다.
    *   구성 가능한 디버그 로깅을 제공합니다.

*   **백엔드 (Spring Boot)**:
    *   Chrome 확장 프로그램에서 프롬프트 데이터를 수신하고 저장합니다.
    *   OpenAI API를 활용하여 프롬프트를 분류합니다(예: "소프트웨어 개발", "일상생활").
    *   다국어 분류(영어 및 한국어)를 지원합니다.
    *   다음과 같은 REST API를 제공합니다:
        *   새 프롬프트 캡처.
        *   사용량 통계 검색 (일별, 주별, 월별).
        *   날짜별 과거 프롬프트 데이터 가져오기.
        *   카테고리별로 필터링된 프롬프트 검색.

*   **프론트엔드 (React)**:
    *   AI 사용량 통계를 시각화하기 위한 대화형 대시보드.
    *   총 요청 수, 사이트별, 모델별, 카테고리별 요청 수를 표시합니다.
    *   시간별 요청 분포를 위한 타임라인 차트를 포함합니다.
    *   일별, 주별, 월별 통계 간 전환을 허용합니다.
    *   날짜별 과거 프롬프트를 볼 수 있는 기록 페이지를 제공합니다.
    *   카테고리 차트에서 드릴다운하여 해당 카테고리 내의 특정 프롬프트 목록을 볼 수 있습니다.
    *   현대적인 다크 테마 UI를 제공합니다.

## 3. 아키텍처

이 애플리케이션은 다중 모듈 프로젝트로 구성됩니다:

*   `backend`: REST API 및 데이터 처리를 제공하는 Spring Boot 애플리케이션 (Java 21, Gradle).
*   `frontend`: 데이터 시각화를 위한 React 단일 페이지 애플리케이션.
*   `chrome-extension`: 데이터 캡처를 위한 브라우저 확장 프로그램 (HTML, CSS, JavaScript).

## 4. 기술 스택

*   **백엔드**: Java 21, Spring Boot 3.x, Gradle, Spring Data MongoDB, OpenAI API (분류용).
*   **프론트엔드**: React, Chart.js, React Router, Axios.
*   **데이터 저장**: MongoDB (로컬 개발용 Docker Compose를 통해).
*   **Chrome 확장 프로그램**: 표준 HTML, CSS, JavaScript.

## 5. 프로젝트 설정 및 실행

### 필수 구성 요소

시작하기 전에 다음이 설치되어 있는지 확인하십시오:

*   **Java Development Kit (JDK) 21 이상**
*   **Gradle** (일반적으로 Spring Boot 프로젝트에 번들로 제공되지만, 접근 가능한지 확인하십시오)
*   **Node.js 및 npm** (또는 yarn)
*   **Docker 및 Docker Compose** (MongoDB 실행용)
*   **Google Chrome** (또는 확장 프로그램용 Chromium 기반 브라우저)
*   **OpenAI API 키**: 프롬프트 분류에 필요합니다.

### 백엔드 설정

1.  **저장소 복제**:
    ```bash
    git clone /Users/iyeongsu/Desktop/ai-tracker/ai_tracker
    cd ai_tracker
    ```
2.  **OpenAI API 키 구성**:
    `backend/src/main/resources/application.properties`를 열고 `YOUR_API_KEY`를 실제 OpenAI API 키로 바꿉니다:
    ```properties
    openai.api.key=sk-YOUR_ACTUAL_OPENAI_API_KEY
    ```
3.  **Docker Compose로 MongoDB 시작**:
    프로젝트 루트 디렉토리(docker-compose.yml이 있는 곳)로 이동하여 다음을 실행합니다:
    ```bash
    docker-compose up -d
    ```
    이렇게 하면 MongoDB 컨테이너와 Mongo Express (MongoDB용 웹 기반 GUI) 컨테이너가 시작됩니다.
    *   MongoDB는 `mongodb://localhost:27017`에서 접근할 수 있습니다.
    *   Mongo Express는 `http://localhost:8081`에서 접근할 수 있습니다 (사용자 이름: `admin`, 비밀번호: `password`).

4.  **백엔드 애플리케이션 실행**:
    프로젝트 루트 디렉토리로 이동하여 Spring Boot 애플리케이션을 실행합니다:
    ```bash
    ./gradlew :backend:bootRun &
    ```
    백엔드는 `http://localhost:11240`에서 시작됩니다.

### 프론트엔드 설정

1.  **의존성 설치**:
    `frontend` 디렉토리로 이동하여 Node.js 의존성을 설치합니다:
    ```bash
    cd frontend
    npm install
    ```
2.  **API 기본 URL 구성**:
    프론트엔드는 `.env` 파일에서 API 기본 URL을 자동으로 읽습니다. `frontend/.env`에 다음 내용이 포함되어 있는지 확인하십시오:
    ```
    REACT_APP_API_BASE_URL=http://localhost:11240/api
    ```
3.  **프론트엔드 애플리케이션 실행**:
    `frontend` 디렉토리에서 개발 서버를 실행합니다:
    ```bash
    npm start &
    ```
    프론트엔드 애플리케이션은 `http://localhost:3000`에서 접근할 수 있습니다.

### Chrome 확장 프로그램 설정

1.  **Chrome에 확장 프로그램 로드**:
    *   Chrome을 열고 `chrome://extensions`로 이동합니다.
    *   "개발자 모드"를 활성화합니다 (오른쪽 상단).
    *   "압축 해제된 확장 프로그램 로드"를 클릭합니다 (왼쪽 상단).
    *   프로젝트에서 `chrome-extension` 디렉토리를 선택합니다.
2.  **API 엔드포인트 구성**:
    확장 프로그램의 API 엔드포인트는 `chrome-extension/src/config.js`에 구성됩니다. 백엔드를 가리키는지 확인하십시오:
    ```javascript
    const config = {
        API_ENDPOINT: "http://localhost:11240/api/data/capture",
        DEBUG_MODE: true // 프로덕션에서는 false로 설정
    };
    ```
3.  **확장 프로그램 새로고침**:
    확장 프로그램 파일 변경 후, `chrome://extensions`로 돌아가서 "AI 활동 추적기" 확장 프로그램의 "새로고침" 버튼(원형 화살표 아이콘)을 클릭합니다.

## 6. 데이터 저장

프롬프트 데이터는 MongoDB 데이터베이스에 저장됩니다. 백엔드는 MongoDB 인스턴스에 연결되며, 로컬 개발용으로 Docker Compose를 통해 편리하게 실행됩니다.

*   **데이터베이스 이름**: `ai_tracker`
*   **컬렉션 이름**: `prompts`
*   **연결 URI (백엔드용)**: `mongodb://admin:password@localhost:27017/ai_tracker?authSource=admin`

다음 방법을 사용하여 데이터를 직접 볼 수 있습니다:
*   **Mongo Express**: 브라우저에서 `http://localhost:8081`에 접속합니다 (사용자 이름: `admin`, 비밀번호: `password`).
*   **MongoDB Compass**: 제공된 URI를 사용하여 연결합니다.
*   **MongoDB Shell**: `docker exec -it ai_tracker_mongodb mongosh -u admin -p password --authenticationDatabase admin`

## 7. 사용법

1.  **모든 서비스 시작**: MongoDB, 백엔드, 프론트엔드가 실행 중이고 Chrome 확장 프로그램이 로드되었는지 확인하십시오.
2.  **AI 서비스 사용**: 지원되는 AI 플랫폼(예: `chat.openai.com`, `chatgpt.com`, `gemini.google.com`)으로 이동합니다.
3.  **프롬프트 입력**: 평소처럼 프롬프트를 입력하고 전송합니다. 확장 프로그램이 백그라운드에서 데이터를 캡처합니다.
4.  **통계 보기**: 브라우저를 열고 `http://localhost:3000`으로 이동합니다.
    *   기본 페이지는 일별 프롬프트를 보여주는 **기록** 보기입니다.
    *   헤더에서 "대시보드"를 클릭하여 사용량 통계를 확인합니다.
    *   대시보드에서 일별, 주별, 월별 보기 간에 전환할 수 있습니다.
    *   "카테고리별 요청" 차트에서 카테고리를 클릭하여 해당 카테고리에 대한 프롬프트 목록을 볼 수 있습니다.

## 8. 기여

자유롭게 저장소를 포크하고, 이슈를 열거나, 풀 리퀘스트를 제출하십시오.
