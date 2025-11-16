---
id: task-010
title: 'Phase 1.7: 프롬프트 조회 API 구현 (TDD)'
status: In Progress
assignee: []
created_date: '2025-09-24 09:42'
updated_date: '2025-10-29 14:37'
labels:
  - phase-1
  - backend
  - opensearch
  - api
  - tdd
dependencies: []
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
OpenSearch에 색인된 프롬프트 데이터를 키워드, 태그, 카테고리 등 다양한 조건으로 조회할 수 있는 REST API를 구현합니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 검색 조건을 담는 'presentation/api/dto/PromptSearchRequest.java' DTO를 정의합니다.
- [ ] #2 검색 요청을 받아 OpenSearch 쿼리를 실행하는 'application/service/PromptQueryService.java'를 구현합니다.
- [ ] #3 GET '/api/v1/prompts/search' 요청을 처리하는 'presentation/api/PromptQueryController.java'를 구현합니다.
- [ ] #4 TDD 사이클에 따라, 서비스 유닛 테스트와 컨트롤러 통합 테스트를 작성하고 통과시킵니다.
- [ ] #5 OpenSearch 통합 테스트에서 복잡한 검색 쿼리가 올바르게 동작하는지 검증합니다.
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
다양한 검색 조건을 처리하는 검색 API 설계 방법을 학습합니다. Spring Data Elasticsearch의 쿼리 메서드 또는 CriteriaQuery를 이용한 복잡한 검색 쿼리 작성법을 익힙니다.
<!-- SECTION:NOTES:END -->
