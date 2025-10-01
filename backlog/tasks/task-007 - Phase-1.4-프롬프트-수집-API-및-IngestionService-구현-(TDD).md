---
id: task-007
title: 'Phase 1.4: 프롬프트 수집 API 및 IngestionService 구현 (TDD)'
status: Done
assignee: []
created_date: '2025-09-24 09:41'
updated_date: '2025-10-01 15:37'
labels:
  - phase-1
  - backend
  - api
  - event
dependencies: []
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
클라이언트로부터 프롬프트 데이터를 수신하는 REST API 엔드포인트를 구현하고, 이를 통해 Prompt를 저장하며 비동기 처리를 위한 'PromptReceivedEvent'를 발행합니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 클라이언트의 요청 본문을 매핑할 `presentation/api/dto/CaptureRequest.java` DTO를 레코드(record)로 정의합니다. `String payload` 필드를 포함해야 합니다.
- [x] #2 @RestController를 사용하여 `presentation/api/DataController.java`를 구현합니다. 이 컨트롤러는 `/api/v1/prompts` 경로로 들어오는 POST 요청을 처리해야 하며, 요청 본문은 `application/json` 타입입니다.
- [x] #3 DataController의 API는 `IngestionService`를 호출한 후, HTTP 상태 코드 202 (Accepted)와 함께 빈 본문을 반환해야 합니다.
- [x] #4 비동기 처리를 위해 `application/event/PromptReceivedEvent.java` 이벤트를 정의합니다. 이 클래스는 생성된 Prompt의 ID(`Long promptId`)를 필드로 가져야 합니다.
- [x] #5 `@Service` 어노테이션을 사용하여 `application/service/IngestionService.java`를 구현합니다. 이 서비스는 `CaptureRequest`를 인자로 받아 `Prompt` 엔티티를 `RECEIVED` 상태로 저장해야 합니다.
- [x] #6 IngestionService는 `PromptRepository`를 사용하여 엔티티를 저장하고, 저장 후에는 `ApplicationEventPublisher`를 통해 `PromptReceivedEvent`를 발행해야 합니다.
- [x] #7 TDD 접근법에 따라 `IngestionService`에 대한 단위 테스트(`IngestionServiceTest.java`)를 작성합니다. `PromptRepository`와 `ApplicationEventPublisher`는 Mockito를 사용하여 모의(mock) 처리합니다.
- [x] #8 `IngestionServiceTest`는 서비스 메소드 호출 시 `PromptRepository.save()`와 `ApplicationEventPublisher.publishEvent()`가 정확히 1회씩 호출되는지 `verify()`를 통해 검증해야 합니다.
- [x] #9 TDD 접근법에 따라 `DataController`에 대한 통합 테스트(`DataControllerTest.java`)를 작성합니다. 이 테스트는 `IntegrationTestSupport`를 상속하고 `@AutoConfigureMockMvc`를 사용해야 합니다.
- [x] #10 `DataControllerTest`는 `MockMvc`를 사용하여 `/api/v1/prompts` 엔드포인트에 POST 요청을 보내고, 응답 상태 코드가 202인지 검증해야 합니다. `IngestionService`는 `@MockBean`으로 등록하여 실제 로직이 실행되지 않도록 합니다.
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. **DTO 및 이벤트 정의 (Define DTO and Event):**
   - `presentation/api/dto/CaptureRequest.java` 파일을 생성하고, `String payload`를 필드로 갖는 Java 레코드로 정의합니다.
   - `application/event/PromptReceivedEvent.java` 파일을 생성하고, `Long promptId`를 필드로 갖는 클래스로 정의합니다.

2. **서비스 계층 TDD (Service Layer TDD):**
   - `test/java/.../application/service/IngestionServiceTest.java` 테스트 클래스를 생성합니다.
   - `@ExtendWith(MockitoExtension.class)`를 사용하여 Mockito를 활성화합니다.
   - `PromptRepository`와 `ApplicationEventPublisher`를 `@Mock`으로 선언합니다.
   - **실패하는 테스트 작성:** `ingest()` 메소드가 호출될 때, `promptRepository.save()`와 `eventPublisher.publishEvent()`가 각각 한 번씩 호출되는지 검증하는 테스트 케이스를 작성합니다. 처음에는 이 테스트가 실패해야 합니다.
   - **구현:** `main/java/.../application/service/IngestionService.java` 클래스와 `ingest()` 메소드를 구현하여 테스트를 통과시킵니다. `Prompt` 객체를 생성하고, 상태를 `RECEIVED`로 설정한 후 저장하고, 이벤트를 발행하는 로직을 포함합니다.

3. **컨트롤러 계층 TDD (Controller Layer TDD):**
   - `test/java/.../presentation/api/DataControllerTest.java` 테스트 클래스를 생성합니다.
   - `IntegrationTestSupport`를 상속하고 `@AutoConfigureMockMvc` 어노테이션을 추가합니다.
   - `MockMvc`를 `@Autowired`로 주입받고, `IngestionService`를 `@MockBean`으로 선언합니다.
   - **실패하는 테스트 작성:** `MockMvc`를 사용하여 `/api/v1/prompts` 엔드포인트로 JSON 본문과 함께 POST 요청을 보냈을 때, 응답 상태 코드가 `202 Accepted`인지 검증하는 테스트 케이스를 작성합니다. 처음에는 이 테스트가 실패해야 합니다.
   - **구현:** `main/java/.../presentation/api/DataController.java` 클래스와 API 엔드포인트 메소드를 구현하여 테스트를 통과시킵니다. `IngestionService`의 `ingest()` 메소드를 호출하고 `ResponseEntity.accepted().build()`를 반환합니다.

4. **최종 검토 및 리팩토링 (Final Review and Refactoring):**
   - 작성된 모든 코드를 검토하여 명확성, 효율성, 그리고 프로젝트 코드 스타일에 맞는지 확인합니다.
   - 불필요한 코드를 제거하고, 가독성을 높이기 위한 리팩토링을 진행합니다.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
REST API의 상태 코드(202 Accepted)의 의미와 DTO-엔티티 변환의 필요성을 학습합니다. 또한, 애플리케이션 내부의 비동기 처리를 위한 Spring Events의 동작 원리를 이해합니다.
<!-- SECTION:NOTES:END -->
