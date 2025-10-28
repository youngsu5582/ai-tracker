package youngsu5582.tool.ai_tracker.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import youngsu5582.tool.ai_tracker.MockEntityFactory;
import youngsu5582.tool.ai_tracker.application.event.PromptReceivedEvent;
import youngsu5582.tool.ai_tracker.domain.category.Category;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptStatus;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisResult;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class AnalysisServiceTests extends IntegrationTestSupport {

    Prompt prompt;

    @BeforeEach
    void setup() {
        prompt = promptRepository.save(MockEntityFactory.createPrompt(PromptStatus.RECEIVED));
    }

    @Test
    @DisplayName("분석 제공자를 통해 프롬프트를 분석해 저장한다.")
    void analyzeCompleteAndChangeComplete() {
        // given
        Mockito.doReturn(AnalysisResult.builder()
                        .category("JPA")
                        .tagList(List.of("Java", "Spring"))
                        .build()).when(promptAnalysisProvider)
                .analyze(anyString(), any(AnalysisMetadata.class));

        // when
        eventPublisher.publishEvent(new PromptReceivedEvent(prompt.getId()));

        // then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(promptRepository.findById(prompt.getId()))
                    .isPresent()
                    .get()
                    .extracting(Prompt::getStatus)
                    .isEqualTo(PromptStatus.COMPLETED);
        });
        assertThat(tagRepository.findAll()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring");

        assertThat(categoryRepository.findAll()).extracting(Category::getName)
                .containsExactly("JPA");
    }

    @Test
    @DisplayName("분석에 실패하면, 실패로 변경한다")
    void analyzeFailAndChangeFailed() {
        // given
        Mockito.doThrow(new RuntimeException("Something Error!")).when(promptAnalysisProvider)
                .analyze(anyString(), any(AnalysisMetadata.class));

        // when
        eventPublisher.publishEvent(new PromptReceivedEvent(prompt.getId()));

        // then
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(promptRepository.findById(prompt.getId()))
                        .isPresent()
                        .get()
                        .extracting(Prompt::getStatus)
                        .isEqualTo(PromptStatus.FAILED));
    }
}