package youngsu5582.tool.ai_tracker.spring_version_study;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.PersistentObjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HibernateTests extends IntegrationTestSupport {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TestEntityRepository testEntityRepository;

    @BeforeEach
    void setup() {
        testEntityRepository.deleteAll();
    }

    @Entity
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;

        public void changeName(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("새로운 엔티티(transient)를 persist하면 영속화된다.")
    void persist_newEntity_succeeds() {
        // given
        TestEntity entity = new TestEntity();
        entity.changeName("initial name");
        testEntityRepository.save(entity);

        assertThat(testEntityRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("ID를 수동 할당한 새 엔티티를 persist하면 예외가 발생할 수 있다 (Spring Boot 3.4+).")
    void persist_newEntityWithId_throwsException() {
        TestEntity entity = new TestEntity(2L, "");
        assertThatThrownBy(() -> testEntityRepository.save(entity))
            .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}