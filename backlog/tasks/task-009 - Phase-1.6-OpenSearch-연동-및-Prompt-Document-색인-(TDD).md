---
id: task-009
title: 'Phase 1.6: OpenSearch 연동 및 Prompt Document 색인 (TDD)'
status: In Progress
assignee: []
created_date: '2025-09-24 09:42'
updated_date: '2025-10-28 13:48'
labels:
  - phase-1
  - backend
  - opensearch
  - search
  - tdd
dependencies: []
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
검색 및 분석을 위해 OpenSearch에 저장될 'PromptDocument' 모델을 정의하고, Spring Data Elasticsearch를 통해 분석된 프롬프트를 OpenSearch에 색인합니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 'infrastructure/persistence/document/PromptDocument.java'를 비정규화된 구조로 정의합니다. (@Document(indexName = "prompts"))
- [x] #2 'infrastructure/persistence/repository/PromptSearchRepository.java' 인터페이스를 'ElasticsearchRepository'를 상속받아 생성합니다.
- [x] #3 'AnalysisService' 로직에 분석 완료 후 'PromptDocument'를 생성하고 'promptSearchRepository.save()'를 호출하여 OpenSearch에 색인하는 코드를 추가합니다.
- [x] #4 'AnalysisService' 통합 테스트에서, 비동기 작업 완료 후 'promptSearchRepository'를 통해 OpenSearch에 문서가 올바르게 색인되었는지 검증하는 로직을 추가합니다.
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
RDB의 정규화된 모델과 검색엔진의 비정규화된 문서 모델의 차이점을 이해합니다. Spring Data Elasticsearch를 이용한 기본적인 OpenSearch 연동 및 데이터 색인 방법을 학습합니다.
<!-- SECTION:NOTES:END -->
