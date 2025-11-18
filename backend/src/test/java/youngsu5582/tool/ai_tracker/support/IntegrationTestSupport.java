package youngsu5582.tool.ai_tracker.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.data.core.OpenSearchOperations;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import youngsu5582.tool.ai_tracker.application.service.AnalysisService;
import youngsu5582.tool.ai_tracker.application.service.IngestionService;
import youngsu5582.tool.ai_tracker.application.service.PromptQueryService;
import youngsu5582.tool.ai_tracker.application.service.PromptQueryTestingService;
import youngsu5582.tool.ai_tracker.domain.category.CategoryRepository;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptRepository;
import youngsu5582.tool.ai_tracker.domain.tag.TagRepository;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.document.PromptDocument;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.repository.PromptSearchRepository;
import youngsu5582.tool.ai_tracker.provider.PromptAnalysisProvider;

import java.util.Map;

@TestExecutionListeners(
        listeners = EventCaptureListener.class,
        mergeMode = MergeMode.MERGE_WITH_DEFAULTS
)
@ActiveProfiles("test")
@Testcontainers
@Import({EventCaptureListener.class, PromptQueryTestingService.class})
@SpringBootTest
public abstract class IntegrationTestSupport {

    private static final String POSTGRES_IMAGE_NAME = "postgres:16.1";
    // 테스트/프로덕션 모두 OpenSearch 3.x 사용 (클라이언트 3.3.0과 정렬)
    private static final String OPENSEARCH_IMAGE_NAME = "opensearchproject/opensearch:3.3.2";

    @ServiceConnection
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
            .waitingFor(Wait.forListeningPort());

    // OpenSearch 는 아직 ServiceConnection 제공 하지 않음
    // @ServiceConnection
    // No ConnectionDetails found for source '@ServiceConnection source for IntegrationTestSupport.opensearch'
    static final OpenSearchContainer<?> OPENSEARCH_CONTAINER = new OpenSearchContainer<>(DockerImageName.parse(OPENSEARCH_IMAGE_NAME))
            .waitingFor(Wait.forListeningPort())
            .withEnv("DISABLE_SECURITY_PLUGIN", "true");

    static {
        OPENSEARCH_CONTAINER.start();
    }

    @BeforeEach
    void ensureIndex() {
        var ops = openSearchOperations.indexOps(PromptDocument.class);

        if (!ops.exists()) {
            // ① 설정
            Map<String, Object> settings = Map.of(
                    "index.number_of_shards", 1,
                    "index.number_of_replicas", 0
            );

            // ② 매핑
            var mapping = ops.createMapping(PromptDocument.class);

            // ③ 설정+매핑으로 인덱스 생성
            ops.create(settings, mapping);
        }

        ops.refresh();
    }

    @Autowired
    protected PromptRepository promptRepository;

    @Autowired
    protected PromptSearchRepository promptSearchRepository;

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

    @Autowired
    protected OpenSearchClient openSearchClient;

    @Autowired
    protected AnalysisService analysisService;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected PromptQueryService promptQueryService;

    @Autowired
    protected OpenSearchOperations openSearchOperations;

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

        // 인덱스 초기화
        var promptIndexOps = openSearchOperations.indexOps(PromptDocument.class);
        if (promptIndexOps.exists()) {
            promptIndexOps.delete();
        }
    }

    private static String resolveIndexName() {
        Document annotation = PromptDocument.class.getAnnotation(Document.class);
        if (annotation == null || !StringUtils.hasText(annotation.indexName())) {
            throw new IllegalStateException("PromptDocument must declare an indexName");
        }
        return annotation.indexName();
    }
}
