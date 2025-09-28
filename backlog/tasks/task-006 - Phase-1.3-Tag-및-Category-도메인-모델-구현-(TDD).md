---
id: task-006
title: 'Phase 1.3: Tag 및 Category 도메인 모델 구현 (TDD)'
status: In Progress
assignee: []
created_date: '2025-09-24 09:41'
updated_date: '2025-09-25 15:54'
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
tags, categories, prompt_tags 테이블에 해당하는 JPA 엔티티와 Repository를 TDD 방식으로 구현하여 프롬프트의 분류 체계를 마련합니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Tag, Category 엔티티 및 각각의 JpaRepository를 구현합니다.
- [x] #2 Category 엔티티에 자기 참조 관계(@ManyToOne)를 설정하여 계층 구조를 구현합니다.
- [x] #3 Prompt와 Tag의 다대다 관계를 표현하기 위한 조인 테이블용 PromptTag 엔티티를 구현합니다. (@IdClass 사용)
- [x] #4 TDD 사이클에 따라, 각 Repository의 저장/조회 및 관계 매핑에 대한 통합 테스트를 작성하고 통과시킵니다.
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
### 다대다(N:M) 관계 구현 전략 논의

이 태스크를 수행하기에 앞서, `Prompt`와 `Tag`의 다대다 관계를 구현하는 두 가지 주요 접근법에 대해 논의하고 최종 전략을 결정했습니다.

#### 접근법 비교

| 구분 | 접근법 A: JPA 연관관계 사용 (채택) | 접근법 B: ID만 직접 저장/조회 |
| :--- | :--- | :--- |
| **객체 그래프 탐색** | **(장점)** `prompt.getPromptTags()`처럼 객체지향적으로 연관 데이터 탐색 가능 | **(단점)** `prompt` 객체만으로는 태그 정보를 알 수 없어 항상 별도 쿼리 필요 |
| **데이터 조회** | **(단점)** N+1 문제 발생 가능성 (Fetch Join 등으로 해결 필요) | **(장점)** N+1 문제가 원천적으로 발생하지 않으며, 쿼리 튜닝이 직관적 |
| **데이터 변경** | **(장점)** Cascade 옵션으로 객체 상태만 변경하면 DB에 자동 반영되어 편리 | **(단점)** 관계를 맺을 때마다 중간 테이블(`prompt_tags`)을 직접 조작해야 함 |
| **복잡성** | **(초기 복잡성 높음)** 어노테이션 매핑이 복잡함<br>** (로직 단순)** 비즈니스 로직은 단순해짐 | **(초기 복잡성 낮음)** 엔티티가 단순해짐<br>** (로직 복잡)** 서비스 계층의 로직이 복잡해짐 |
| **결합도** | **(높음)** 엔티티 간 결합도가 강해짐 | **(낮음)** 엔티티 간 결합도가 낮아 마이크로서비스 전환 시 유리 |

#### 최종 결정 및 이유

- **결정**: **접근법 A (JPA 연관관계 사용)** 를 채택합니다.
- **이유**:
    1.  **객체지향적 설계**: 현재 단계(잘 구조화된 모놀리식)에서는 도메인 객체 간의 관계를 풍부하게 표현하는 것이 DDD 학습 목표에 부합합니다.
    2.  **생산성 및 학습 기회**: JPA의 Cascade, 연관관계 편의 메소드 등을 활용하여 생산성을 높일 수 있습니다. 또한, 이 과정에서 발생할 수 있는 N+1 문제를 Fetch Join 등으로 해결하는 경험은 JPA 활용 능력을 향상시키는 좋은 학습 기회가 됩니다.
    3.  **미래 고려**: 접근법 B는 서비스가 분리되는 마이크로서비스 단계(Phase 3)에서 성능 최적화를 위해 재검토할 가치가 있는 전략입니다.
<!-- SECTION:NOTES:END -->
