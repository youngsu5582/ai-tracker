package youngsu5582.tool.ai_tracker.spring_version_study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class HibernateTests extends IntegrationTestSupport {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TestEntityRepository testEntityRepository;

    @BeforeEach
    void setup() {
        // deleteAll : SELECT 로 엔티티 조회 -> 엔티티 상태 관리 -> DELETE 쿼리 N번 실행 ( 내부 반복문 )
        // deleteAllInBatch : DB 에 직접 벌크 연산, JPA 생명주기 무시 + 영속 컨텍스트 불일치
        testEntityRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("새로운 엔티티(transient)를 persist하면 영속화된다.")
    void persist_newEntity_succeeds() {
        // given
        TestEntity entity = new TestEntity();
        entity.changeName("initial name");
        var saved = testEntityRepository.saveAndFlush(entity);
        assertThat(saved.getId()).isNotNull();
        assertThat(testEntityRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("ID를 수동 할당한 새 엔티티를 persist하면 예외가 발생할 수 있다 (Spring Boot 3.4+).")
    void persist_newEntityWithId_throwsException() {
        TestEntity entity = new TestEntity(2L, "");
        assertThatThrownBy(() -> testEntityRepository.save(entity))
            .isInstanceOfAny(ObjectOptimisticLockingFailureException.class, PersistenceException.class)
            // check root exception
            .hasRootCauseInstanceOf(StaleObjectStateException.class);
    }
}