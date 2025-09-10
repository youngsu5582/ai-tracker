package youngsu5582.tool.ai_tracker;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class AiTrackerApplicationTests extends IntegrationTestSupport {

    @Autowired
    ApplicationContext applicationContext;

    @PersistenceContext
    private EntityManager entityManager;

    // 스프링 컨텍스트 확인용 테스트 코드
    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    // EntityManager 연결상태 확인용 테스트 코드
    @Test
    void databaseConnectionHealthCheck() {
        assertThat(entityManager).isNotNull();
        assertThat(entityManager.isOpen()).isTrue();
    }

}