---
id: task-008
title: 'Phase 1.5: Prompt Analysis Service 구현 (TDD)'
status: In Progress
assignee: []
created_date: '2025-09-24 09:42'
updated_date: '2025-10-18 14:26'
labels:
  - phase-1
  - backend
  - event
  - async
  - tdd
dependencies: []
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
'PromptReceivedEvent'를 비동기적으로 수신하여 외부 AI API로 프롬프트를 분석하고, 분석 결과를 DB에 업데이트하는 'AnalysisService'를 구현합니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 '@EventListener'와 '@Async'를 사용하여 이벤트를 비동기적으로 처리하는 'AnalysisService' 메서드를 정의합니다. (메인 클래스에 @EnableAsync 추가)
- [x] #2 메서드 전체를 '@Transactional'로 묶어 DB 업데이트의 원자성을 보장합니다.
- [x] #3 외부 OpenAI API 호출을 시뮬레이션하는 'infrastructure/external/OpenAiClient.java' Mock 인터페이스를 생성합니다.
- [x] #4 'AnalysisService' 통합 테스트에서 'ApplicationEventPublisher'로 이벤트를 직접 발행합니다.
- [ ] #5 'Awaitility' 라이브러리를 사용하여 비동기 작업이 완료될 때까지 대기한 후, Prompt의 상태가 'COMPLETED'로 변경되었는지 검증합니다.
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Spring의 비동기 처리(@Async)와 트랜잭션(@Transactional) 동작 방식을 학습합니다. 또한, 통합 테스트에서 외부 API 의존성을 '@MockBean'으로 제거하는 방법과 'Awaitility'를 이용한 비동기 테스트 방법을 익힙니다.
<!-- SECTION:NOTES:END -->
