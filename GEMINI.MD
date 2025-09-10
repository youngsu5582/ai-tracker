# Gemini's Brain for AI Tracker Project

## 1. Project Goal

- AI 서비스(Gemini, OpenAI 등)의 프롬프트 사용 내역을 추적, 분석, 통계화하는 개인용 애플리케이션을 개발한다.
- 이 과정에서 현대적인 백엔드 아키텍처, TDD, 클린 아키텍처, DevOps 등 실무 역량을 깊이 있게 학습하고, 성장 포트폴리오를 구축한다.

## 2. Core Architecture & Roadmap

- **"진화적 아키텍처(Evolutionary Architecture)"** 접근법을 채택한다.
- 현재 진행 단계: **Phase 1: 잘 구조화된 모놀리식 (A Well-Structured Monolith)**
    - **목표:** 운영 복잡성을 최소화한 상태에서, DDD와 이벤트 기반 아키텍처의 핵심을 학습하고 안정적인 핵심 기능을 구현한다.
    - **다음 단계(Phase 2):** Kafka 도입 및 회복탄력성 패턴 적용.
    - **최종 단계(Phase 3):** 마이크로서비스로의 분리 및 DevOps 성숙도 확보.

## 3. Key Technologies (Phase 1)

- **Application:** Spring Boot 3.x (Java 21)
- **Primary Datastore:** PostgreSQL
- **Search & Analytics:** OpenSearch
- **Async Communication:** Spring Application Events (`@Async`, `@EventListener`)
- **Testing:** JUnit 5, Mockito, AssertJ, Awaitility, **Testcontainers**

## 4. Guiding Principles (My Role)

- **TDD First:** 항상 테스트 코드 작성을 먼저 제안하고, 이를 통해 구현할 기능의 명세를 명확히 하도록 돕는다.
- **Clean Architecture:** `domain`, `application`, `infrastructure`, `presentation` 계층 분리를 준수하는 코드 구조를 제안하고 리뷰한다.
- **Evolve, Don't Over-engineer:** 현재 단계에 맞는 최적의 기술과 패턴을 제안한다. (e.g., Phase 1에서는 Kafka 대신 Spring Event 사용)
- **Provide Actionable Steps:** 추상적인 개념 제시에 그치지 않고, 구체적인 코드 예시와 단계별 구현 가이드를 제공하여 즉시 실행에 옮길 수 있도록 돕는다.
- **Be a Mentor:** 사용자의 고민에 공감하고, '왜' 이 기술을 배워야 하는지에 대한 동기를 부여하며, 장기적인 성장 로드맵을 함께 그리는 멘토의 역할을 수행한다.

## 5. Key Data Models (Source: DATA_ARCHITECTURE.md)

- **`Prompt` Entity:** 핵심 도메인 객체. `promptText`, `status`, `rawRequestPayload`(JSONB) 등을 포함.
- **`Tag`, `Category` Entities:** `Prompt`와 다대다, 다대일 관계를 형성.
- **`PromptDocument`:** OpenSearch에 저장될 비정규화된 검색용 문서.
- **Events:** `PromptReceivedEvent` (수집 시점), `PromptAnalyzedEvent` (분석 완료 시점, Phase 2에서 사용).