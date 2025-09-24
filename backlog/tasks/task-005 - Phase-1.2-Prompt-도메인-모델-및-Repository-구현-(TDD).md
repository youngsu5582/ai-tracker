---
id: task-005
title: 'Phase 1.2: Prompt 도메인 모델 및 Repository 구현 (TDD)'
status: In Progress
assignee: []
created_date: '2025-09-24 09:40'
updated_date: '2025-09-24 15:10'
labels:
  - phase-1
  - backend
  - database
  - jpa
  - tdd
dependencies: []
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
DATA_ARCHITECTURE.md에 정의된 'prompts' 테이블에 해당하는 JPA 엔티티와 Spring Data JPA Repository를 TDD 방식으로 구현합니다. 이는 우리 시스템 데이터의 가장 기본이 되는 핵심 모델을 정의하는 과정입니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 'domain/prompt/PromptStatus.java' Enum (RECEIVED, ANALYZING, COMPLETED, FAILED)을 정의합니다.
- [x] #2 'domain/prompt/Prompt.java' 엔티티를 @Entity를 사용해 'prompts' 테이블과 매핑합니다. (UUID PK, JSONB, @Enumerated, @CreatedDate 등 포함)
- [x] #3 'domain/prompt/PromptRepository.java' 인터페이스를 'JpaRepository<Prompt, UUID>'를 상속받아 생성합니다.
- [x] #4 TDD 사이클에 따라, 'Prompt' 저장 및 조회 기능에 대한 통합 테스트('PromptRepositoryTest')를 먼저 작성하고 통과시킵니다.
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
JPA 엔티티 매핑, 특히 UUID 기본 키와 JSONB 타입 매핑 방법을 중점적으로 학습합니다. 또한, 테스트 코드를 먼저 작성하고 실제 코드를 구현하는 TDD의 실용적인 흐름을 익힙니다.
<!-- SECTION:NOTES:END -->
