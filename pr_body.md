## 📝 Related Task

- Closes #task-005

## 📄 Description

`DATA_ARCHITECTURE.md`에 정의된 핵심 데이터 모델인 `Prompt` 엔티티와, 데이터베이스 상호작용을 위한 `PromptRepository`를 TDD(Test-Driven Development) 방식으로 구현했습니다.

이는 우리 시스템 데이터 모델의 가장 기본이 되는 뼈대를 구축하는 작업이며, 향후 모든 프롬프트 관련 데이터 처리는 이 모델을 기반으로 이루어집니다.

## ✨ Key Changes

이번 PR의 주요 변경 사항은 다음과 같습니다.

- **`PromptStatus` Enum 정의**: 프롬프트의 상태(`RECEIVED`, `ANALYZING`, `COMPLETED`, `FAILED`)를 관리하기 위한 Enum 타입을 추가했습니다.
- **`Prompt` 엔티티 구현**:
    - `@Entity`를 사용하여 `prompts` 테이블과 매핑했습니다.
    - Primary Key로 `UUID`를 사용하도록 `@GeneratedValue(strategy = GenerationType.UUID)`를 적용했습니다.
    - 비정형 데이터를 유연하게 다루기 위해 `payload` 필드를 PostgreSQL의 `JSONB` 타입과 매핑했습니다. (`@JdbcTypeCode(SqlTypes.JSON)`)
    - 생성/수정 시간을 자동으로 기록하기 위해 `@EntityListeners(AuditingEntityListener.class)`를 적용했습니다.
- **`PromptRepository` 인터페이스 생성**: Spring Data JPA의 `JpaRepository<Prompt, UUID>`를 상속받아 기본적인 CRUD 기능을 확보했습니다.
- **TDD 기반 통합 테스트 작성**:
    - `PromptRepositoryTest`를 작성하여 TDD 사이클을 준수했습니다.
    - `Testcontainers` 환경에서 실제 PostgreSQL 데이터베이스를 대상으로 엔티티의 저장 및 조회 기능이 올바르게 동작하는지 검증했습니다.

## 🤖 To Reviewers (especially AI CodeRabbit)

리뷰 시 아래 사항들을 중점적으로 확인해 주시면 감사하겠습니다.

- **JPA Annotation Correctness**: `Prompt` 엔티티에 적용된 JPA 어노테이션(`@Id`, `@GeneratedValue`, `@Column`, `@JdbcTypeCode` 등)이 의도에 맞게 정확히 사용되었는지 확인 부탁드립니다.
- **TDD Principle**: 테스트 코드(`PromptRepositoryTest`)가 실제 구현 코드보다 먼저 개념적으로 정의되고, 구현을 검증하는 역할을 충실히 수행하는지 확인 부탁드립니다.
- **Immutability & Encapsulation**: 엔티티의 필드들이 불필요하게 외부로 노출되거나 변경 가능하지 않은지, 객체지향 원칙에 따라 잘 캡슐화되었는지 검토 부탁드립니다.

## ✅ Checklist

- [x] 제 코드에 대한 자체 리뷰를 수행했습니다.
- [x] 이 프로젝트의 스타일 가이드라인을 따랐습니다.
- [x] 변경 사항에 대한 테스트 코드를 작성했습니다.
- [x] 모든 신규 및 기존 테스트를 통과했습니다.