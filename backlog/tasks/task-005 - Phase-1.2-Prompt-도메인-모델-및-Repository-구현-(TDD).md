---
id: task-005
title: 'Phase 1.2: Prompt 도메인 모델 및 Repository 구현 (TDD)'
status: Done
assignee: []
created_date: '2025-09-24 09:40'
updated_date: '2025-09-25 14:29'
labels:
  - phase-1
  - backend
  - database
  - jpa
  - tdd
dependencies: []
priority: medium
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
DATA_ARCHITECTURE.md에 정의된 'prompts' 테이블에 해당하는 JPA 엔티티와 Spring Data JPA Repository를 TDD 방식으로 구현합니다. 이는 우리 시스템 데이터의 가장 기본이 되는 핵심 모델을 정의하는 과정입니다.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 'domain/prompt/PromptStatus.java' Enum (RECEIVED, ANALYZING, COMPLETED, FAILED)을 정의합니다.
- [x] #2 'domain/prompt/Prompt.java' 엔티티를 @Entity를 사용해 'prompts' 테이블과 매핑합니다. (Long IDENTITY PK, UUID business key(unique, not null), JSONB, @Enumerated, @CreatedDate 등 포함)
- [x] #3 'domain/prompt/PromptRepository.java' 인터페이스를 'JpaRepository<Prompt, Long>'를 상속받아 생성합니다.
- [x] #4 TDD 사이클에 따라, 'Prompt' 저장 및 조회 기능에 대한 통합 테스트('PromptRepositoryTest')를 먼저 작성하고 통과시킵니다.
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
### Long ID vs UUID 기본 키(PK) 비교 및 학습

회사의 표준인 `Long` 타입 ID 전략을 존중하면서, `UUID`의 장점을 함께 활용하는 하이브리드 전략에 대한 상세 설명입니다.

#### Long ID vs UUID 비교표

| 구분 | Long ID (시퀀스 정수) | UUID (범용 고유 식별자) |
| :--- | :--- | :--- |
| **성능** | **(장점)** 순차적 값으로 B-Tree 인덱스에 최적화되어 인덱싱 및 조인 성능이 빠릅니다. | **(단점)** 랜덤 값(v4)은 인덱스 단편화를 유발하여 쓰기 성능 저하의 원인이 될 수 있습니다. |
| **저장 공간** | **(장점)** 8바이트를 사용하여 UUID(16바이트) 대비 50%의 공간을 절약합니다. | **(단점)** 16바이트를 사용하여 더 많은 공간과 캐시 메모리를 차지합니다. |
| **고유성** | **(단점)** 단일 DB 내에서만 고유성이 보장됩니다. | **(장점)** 전 세계적으로 고유하여 분산 시스템 및 DB 통합 시 ID 충돌이 없습니다. |
| **보안** | **(단점)** 예측 가능한 시퀀스로 Enumeration Attack(다른 리소스 추측)에 취약할 수 있습니다. | **(장점)** 추측이 불가능하여 URL 등에 사용해도 안전합니다. |
| **분산 환경** | **(단점)** 여러 서버에서 ID 생성 시 충돌 방지를 위한 별도 매커니즘이 필요합니다. | **(장점)** 어떤 서버에서든 독립적으로 ID 생성이 가능하여 마이크로서비스에 적합합니다. |
| **가독성** | **(장점)** 사람이 읽고 디버깅하기 쉽습니다. | **(단점)** 길고 복잡하여 디버깅 시 다루기 어렵습니다. |

#### PostgreSQL 특별 고려사항

- PostgreSQL의 `BIGINT GENERATED ... AS IDENTITY`는 시퀀스 ID 생성을 위해 고도로 최적화되어 있습니다.
- **UUID v7** (시간 기반)을 사용하면 v4의 인덱스 성능 저하 문제를 크게 완화할 수 있습니다.

#### 현재 코드 문제점 및 해결책

- **문제**: `PromptRepository<Prompt, UUID>`와 `Prompt` 엔티티의 `long id` PK 간의 타입 불일치.
- **해결책**: `long` ID를 PK로 유지하고 Repository를 `JpaRepository<Prompt, Long>`으로 수정합니다. `UUID` 필드는 외부 통신 등을 위한 고유 비즈니스 식별자로 계속 활용합니다. 이 하이브리드 전략은 성능과 안전성을 모두 고려한 좋은 접근법입니다.

### 인덱스 카디널리티에 대한 학습 내용 (status 컬럼)
- **카디널리티와 인덱스**: 일반적으로 카디널리티(값의 종류)가 낮은 컬럼의 인덱스는 비효율적일 수 있지만, 항상 그런 것은 아닙니다.
- **데이터 분포의 중요성**: 값의 종류가 적더라도 데이터 분포가 한쪽으로 치우쳐 있다면(예: 99%는 COMPLETED, 1%는 ANALYZING), 소수의 데이터를 찾는 쿼리에서 인덱스는 매우 효율적입니다.
- **핵심 조회 패턴 고려**: `findByStatus()`와 같이 자주 사용되는 조회 조건이라면 인덱스 생성을 긍정적으로 검토해야 합니다.
- **실용적 접근법**: 성능이 우려된다면, 처음에는 인덱스 없이 배포하고 실제 쿼리 성능을 측정한 후 필요할 때 추가하는 것도 좋은 전략입니다.
- **우선순위**: `status` 인덱스는 성능 최적화의 영역이지만, `uuid`의 고유(unique) 제약은 데이터 무결성의 영역이므로 훨씬 더 중요합니다.

### 'status' 컬럼 인덱싱에 대한 심층 논의

- **고민**: `status` 컬럼은 카디널리티가 낮고(값의 종류가 적고), 상태 변경으로 `UPDATE`가 빈번하여 인덱싱의 효율성에 대한 우려가 있었습니다.
- **분석**:
    - **쓰기 비용**: `UPDATE` 시 인덱스 수정 비용이 발생하지만, 인덱스 크기가 작아 대부분의 경우 오버헤드는 미미합니다.
    - **읽기 이점**: 반면, 데이터 분포가 불균등할 것(대부분 `COMPLETED`, 소수 `ANALYZING`)으로 예상되므로, `ANALYZING` 등 처리 대상을 찾는 핵심 쿼리에서 인덱스는 Full Table Scan을 방지하여 시스템 전체 성능에 결정적인 이점을 제공합니다.
- **최종 전략**: **초기에는 인덱스를 포함하는 것을 권장합니다.** 읽기 성능 향상으로 얻는 이점이 쓰기 시의 작은 비용보다 훨씬 크다고 판단됩니다. 단, 가장 확실한 방법은 '측정 후 결정'이므로, 실제 운영에서 `UPDATE` 성능 병목이 데이터로 확인될 경우 인덱스를 제거하는 것을 고려할 수 있습니다.
<!-- SECTION:NOTES:END -->
