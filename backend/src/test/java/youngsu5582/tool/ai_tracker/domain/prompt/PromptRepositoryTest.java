package youngsu5582.tool.ai_tracker.domain.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import youngsu5582.tool.ai_tracker.MockEntityFactory;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;
import youngsu5582.tool.ai_tracker.domain.tag.TagRepository;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class PromptRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private PromptRepository promptRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    Prompt prompt;

    @BeforeEach
    void setUp() {
        prompt = promptRepository.save(MockEntityFactory.createPrompt(PromptStatus.COMPLETED));
    }

    @Nested
    @DisplayName("Prompt 레포지토리 조회는")
    class PromptRepositoryOnlyTests {


        @BeforeEach
        void setUp() {

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
            var result = promptRepository.findByUuid(prompt.getUuid());
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

        @Test
        @DisplayName("삭제는 실제 삭제가 되는게 아니다.")
        void softDelete() {
            promptRepository.delete(prompt);
            promptRepository.flush();

            // JPA 조회
            var findResult = promptRepository.findByUuid(prompt.getUuid());
            assertThat(findResult).isEmpty();

            // 직접 조회
            Object isDeleted = entityManager.createNativeQuery(
                    "SELECT deleted FROM prompts WHERE uuid = :uuid")
                .setParameter("uuid", prompt.getUuid())
                .getSingleResult();

            assertThat(isDeleted).isEqualTo(true);
        }

    }

    @Nested
    @DisplayName("프롬프트와 태그는")
    class PromptRepositoryWithTags {

        @Test
        @Transactional
        @DisplayName("1:N 관계로 관리한다.")
        void oneToManyWithPromptTags() {
            Tag tag1 = MockEntityFactory.createTag("spring");
            Tag tag2 = MockEntityFactory.createTag("java");
            tagRepository.saveAll(List.of(tag1, tag2));
            prompt.addTag(tag1);
            prompt.addTag(tag2);

            entityManager.flush();
            entityManager.clear();

            var findResult = promptRepository.findByUuid(prompt.getUuid());
            assertThat(findResult)
                .get()
                .extracting(Prompt::getPromptTags, LIST)
                .extracting("tag")
                .containsExactlyInAnyOrder(tag1, tag2);
        }
    }
}