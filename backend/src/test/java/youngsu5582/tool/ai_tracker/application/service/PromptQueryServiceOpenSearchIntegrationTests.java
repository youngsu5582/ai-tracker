package youngsu5582.tool.ai_tracker.application.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.IndexOperations;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchCommand;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.document.PromptDocument;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptQueryServiceOpenSearchIntegrationTests extends IntegrationTestSupport {

    @Autowired
    PromptQueryTestingService promptQueryTestingService;

    @BeforeEach
    void setup() {
        initData();
    }

    @AfterEach
    void tearDown() {
        deleteData();
    }

    @Test
    @DisplayName("OpenSearch에 저장된 문서를 키워드와 태그, 카테고리 조건으로 필터링한다.")
    void search_withKeywordAndTags_filtersDocuments() {
        PromptSearchCommand command = new PromptSearchCommand(
                "latency",
                Instant.parse("2024-04-01T00:00:00Z"),
                Instant.parse("2024-06-01T00:00:00Z"));
        // when & then

        PromptSearchResult result = promptQueryService.search(command);
        assertThat(result.total()).isEqualTo(3L);
        assertThat(result.prompts()).hasSize(3);
        assertThat(result.prompts()).extracting(PromptSearchResult.PromptSummary::messageId)
                .contains("message-1", "message-2", "message-3");
    }

    @Test
    @DisplayName("모든 요소를 검색한다")
    void search_all() {
        List<PromptDocument> promptDocumentList = promptQueryTestingService.searchAll();
        assertThat(promptDocumentList).hasSize(3);
    }

    @Test
    @DisplayName("카테고리로 검색한다")
    void search_category() {
        List<PromptDocument> promptDocumentList = promptQueryTestingService.searchByCategory("Generative AI");
        assertThat(promptDocumentList).hasSize(1)
                .extracting(PromptDocument::getId)
                .contains("uuid-1");
    }

    @Test
    @DisplayName("태그로 검색한다")
    void search_tag() {
        List<PromptDocument> promptDocumentList = promptQueryTestingService.searchWithTag("otel");
        assertThat(promptDocumentList).hasSize(1)
                .extracting(PromptDocument::getId)
                .contains("uuid-3");
    }

    @Test
    @DisplayName("페이로드로 검색한다")
    void search_payload() {
        List<PromptDocument> promptDocumentList = promptQueryTestingService.searchWithPayload("for");
        assertThat(promptDocumentList).hasSize(2)
                .extracting(PromptDocument::getId)
                .contains("uuid-2", "uuid-3");
    }

    private void initData() {
        // given
        promptSearchRepository.saveAll(
                List.of(
                        PromptDocument.builder()
                                .id("uuid-1")
                                .promptId(1L)
                                .messageId("message-1")
                                .provider("OPENAI")
                                .status("COMPLETED")
                                .payload("Investigate latency patterns with java prompt-engineering techniques.")
                                .category("Generative AI")
                                .parentCategory("Architecture")
                                .tags(List.of("java", "prompt-engineering"))
                                .createdAt(Instant.parse("2024-05-01T10:00:00Z"))
                                .analyzedAt(Instant.parse("2024-05-01T11:00:00Z"))
                                .build(),
                        PromptDocument.builder()
                                .id("uuid-2")
                                .promptId(2L)
                                .messageId("message-2")
                                .provider("OPENAI")
                                .status("FAILED")
                                .payload("Explore caching for throughput optimisation.")
                                .category("latency")
                                .tags(List.of("spring", "performance"))
                                .createdAt(Instant.parse("2024-05-01T12:00:00Z"))
                                .analyzedAt(Instant.parse("2024-05-01T13:00:00Z"))
                                .build(),
                        PromptDocument.builder()
                                .id("uuid-3")
                                .promptId(3L)
                                .messageId("message-3")
                                .provider("ANTHROPIC")
                                .status("COMPLETED")
                                .payload("analysis for distributed tracing pipelines.")
                                .category("Observability")
                                .tags(List.of("otel", "latency"))
                                .createdAt(Instant.parse("2024-05-02T09:00:00Z"))
                                .analyzedAt(Instant.parse("2024-05-02T09:30:00Z"))
                                .build()
                ));


        IndexOperations indexOps = openSearchOperations.indexOps(PromptDocument.class);
        indexOps.refresh();
    }

    private void deleteData() {
        promptQueryTestingService.deleteAllDocuments();
    }
}
