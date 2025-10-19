package youngsu5582.tool.ai_tracker.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata.AnalysisMetadataAttribute;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisResult;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class OpenAiPromptAnalysisProviderTests extends IntegrationTestSupport {

    private ChatClient mockChatClient;
    private OpenAiPromptAnalysisProvider openAiPromptAnalysisProvider;


    @BeforeEach
    void setup() {
        mockChatClient = mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        ChatClient.Builder mockBuilder = mock(ChatClient.Builder.class);

        when(mockBuilder.build()).thenReturn(mockChatClient);
        openAiPromptAnalysisProvider = new OpenAiPromptAnalysisProvider(
            mockBuilder);
    }

    @Test
    @DisplayName("주어진 프롬프트와 메타데이터를 사용해 OpenAI API 호출 후 결과를 올바르게 파싱한다")
    void analyzePromptAndParseResult() {
        // given
        // 예상되는 AI의 응답 (JSON 형태의 문자열)을 정의
        String expectedJson = """
            {
                "parentCategory": "Backend",
                "category": "Spring Boot",
                "tagList": ["java", "spring", "jpa"]
            }
            """;

        // mockChatClient의 실제 호출 체인에 대해 예상 응답을 반환하도록 설정
        when(mockChatClient.prompt(any(Prompt.class)).call().content()).thenReturn(expectedJson);

        // 테스트에 사용할 입력 데이터 준비
        String payload = "Spring Boot JPA에서 N+1 문제를 해결하는 방법에 대해 알려줘";
        AnalysisMetadata metadata = new AnalysisMetadata(
            Map.of(AnalysisMetadataAttribute.TAG_LIST, List.of("Java", "Spring Boot", "Database"),
                AnalysisMetadataAttribute.CATEGORY_LIST, List.of("jpa", "querydsl", "n+1"))
        );

        // when
        AnalysisResult actualResult = openAiPromptAnalysisProvider.analyze(payload, metadata);

        // then
        // 반환된 결과가 예상과 일치하는지 검증
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.parentCategory()).isEqualTo("Backend");
        assertThat(actualResult.category()).isEqualTo("Spring Boot");
        assertThat(actualResult.tagList()).containsExactlyInAnyOrder("java", "spring", "jpa");
    }
}
