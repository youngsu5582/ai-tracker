package youngsu5582.tool.ai_tracker.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import youngsu5582.tool.ai_tracker.application.service.IngestionService;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptRepository;

@TestExecutionListeners(
    listeners = EventCaptureListener.class,
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS
)
@ActiveProfiles("test")
@Import(EventCaptureListener.class)
@SpringBootTest
public abstract class IntegrationTestSupport {

    private static final String POSTGRES_IMAGE_NAME = "postgres:16.1";

    @ServiceConnection
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(
        POSTGRES_IMAGE_NAME)
        .waitingFor(Wait.forListeningPort());

    @Autowired
    protected PromptRepository promptRepository;

    @Autowired
    protected EventCaptureListener eventCaptureListener;

    @Autowired
    protected IngestionService ingestionService;

    @PersistenceContext
    protected EntityManager entityManager;
}
