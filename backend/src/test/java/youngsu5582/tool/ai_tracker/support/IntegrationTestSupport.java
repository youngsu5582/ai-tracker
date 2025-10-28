package youngsu5582.tool.ai_tracker.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import youngsu5582.tool.ai_tracker.application.service.AnalysisService;
import youngsu5582.tool.ai_tracker.application.service.IngestionService;
import youngsu5582.tool.ai_tracker.domain.category.CategoryRepository;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptRepository;
import youngsu5582.tool.ai_tracker.domain.tag.TagRepository;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.repository.PromptSearchRepository;
import youngsu5582.tool.ai_tracker.provider.PromptAnalysisProvider;

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
    protected CategoryRepository categoryRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Autowired
    protected EventCaptureListener eventCaptureListener;

    @Autowired
    protected IngestionService ingestionService;

    @MockitoBean
    protected PromptAnalysisProvider promptAnalysisProvider;

    @MockitoBean
    protected PromptSearchRepository promptSearchRepository;

    @Autowired
    protected AnalysisService analysisService;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void setup() {
        // PostgreSQL의 BASE TABLE만 조회 (VIEW는 제외)
        var tableNames = jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
            String.class
        );

        if (!tableNames.isEmpty()) {
            // db 컨텍스트를 공유하는 테스트가 있어서 테스트 하기 전 로우들 청소
            jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tableNames) + " CASCADE");
        }
    }
}
