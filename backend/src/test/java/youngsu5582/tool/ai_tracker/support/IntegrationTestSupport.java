package youngsu5582.tool.ai_tracker.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

    private static final String POSTGRES_IMAGE_NAME = "postgres:16.1";

    @ServiceConnection
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(
        POSTGRES_IMAGE_NAME)
        .waitingFor(Wait.forListeningPort());
}
