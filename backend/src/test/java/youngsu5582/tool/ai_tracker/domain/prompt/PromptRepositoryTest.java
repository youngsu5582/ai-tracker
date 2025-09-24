package youngsu5582.tool.ai_tracker.domain.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import youngsu5582.tool.ai_tracker.MockEntityFactory;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class PromptRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private PromptRepository promptRepository;

    @Nested
    @DisplayName("Prompt 레포지토리 조회는")
    class PromptRepositoryRead {

        Prompt prompt;

        @BeforeEach
        void setUp() {
            prompt = promptRepository.save(MockEntityFactory.createPrompt(PromptStatus.COMPLETED));

            // 완료 2개, 분석중 1개, 수신 1개, 실패 1개를 추가한다.
            promptRepository.saveAll(
                List.of(
                    MockEntityFactory.createPrompt(PromptStatus.COMPLETED),
                    MockEntityFactory.createPrompt(PromptStatus.ANALYZING),
                    MockEntityFactory.createPrompt(PromptStatus.FAILED),
                    MockEntityFactory.createPrompt(PromptStatus.COMPLETED),
                    MockEntityFactory.createPrompt(PromptStatus.RECEIVED)
                )
            );
        }

        @AfterEach
        void tearDown() {
            promptRepository.deleteAll();
        }

        @Test
        @DisplayName("UUID 로 조회할 수 있다.")
        void findByUUID() {
            var result = promptRepository.findByUuid(prompt.uuid);
            assertThat(result).isPresent()
                .get()
                .isEqualTo(prompt);
        }

        @Test
        @DisplayName("상태로 조회할 수 있다.")
        void findByStatus() {
            SoftAssertions.assertSoftly(softly -> {
                var completeResult = promptRepository.findByStatus(PromptStatus.COMPLETED);
                assertThat(completeResult).hasSize(3);

                var analyzingResult = promptRepository.findByStatus(PromptStatus.ANALYZING);
                assertThat(analyzingResult).hasSize(1);

                var failedResult = promptRepository.findByStatus(PromptStatus.FAILED);
                assertThat(failedResult).hasSize(1);

                var receivedResult = promptRepository.findByStatus(PromptStatus.RECEIVED);
                assertThat(receivedResult).hasSize(1);
            });
        }
    }

}