package youngsu5582.tool.ai_tracker.provider;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisResult;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.ai.openai.api-key")
public class OpenAiPromptAnalysisProvider implements PromptAnalysisProvider {

    private final ChatClient chatClient;

    @PostConstruct
    public void init() {
        log.info("Open AI 프롬프트 분석이 활성화되었습니다!");
    }

    public OpenAiPromptAnalysisProvider(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    private static final String SYSTEM_PROMPT = """
        당신은 사용자의 프롬프트 내용을 분석하여 적절한 카테고리와 태그를 추천해주는 전문 AI 어시스턴트입니다.

        분석의 정확도를 높이기 위해, 현재 시스템에 존재하는 카테고리 및 태그 목록을 제공합니다.
        - EXISTING_CATEGORIES: [{existing_categories}]
        - EXISTING_TAGS: [{existing_tags}]

        ## 중요 규칙
        1. **카테고리 선택**: 먼저, 프롬프트 내용에 가장 적합한 카테고리를 `EXISTING_CATEGORIES` 목록에서 찾아보세요. 만약 적절한 것이 있다면 해당 카테고리 이름을 `category` 값으로 사용하세요. 적절한 것이 없다면, 주제에 맞는 새로운 카테고리 이름을 생성하세요.
        2. **태그 선택**: `tagList`를 생성할 때, `EXISTING_TAGS` 목록에 있는 태그가 내용과 관련 있다면 최대한 재사용해주세요. 물론, 필요하다면 목록에 없는 새로운 태그를 추가하는 것도 좋습니다. 기존 태그와 새로운 태그를 조합할 수 있습니다.
        3. **상위 카테고리**: `parentCategory`는 `category`의 상위 개념으로 'EXISTING_CATEGORIES' 또는 주제에 맞는 새로운 카테고리를 생성하세요.

        결과는 반드시 다음 JSON 형식으로만 응답해야 합니다:
        {format}
        """;

    private static final String USER_PROMPT = """
        다음 프롬프트 내용을 분석해주세요:
        ---
        {payload}
        ---
        """;

    /**
     * 인지한 에러 목록
     * <p>
     * org.springframework.ai.retry.NonTransientAiException: HTTP 401 - { "error": { "message":
     * "Incorrect API key provided:
     * sk-proj-***************************************************************cYkC. You can find
     * your API key at https://platform.openai.com/account/api-keys.", "type":
     * "invalid_request_error", "param": null, "code": "invalid_api_key" } }
     */
    @Override
    public AnalysisResult analyze(String payload, AnalysisMetadata analysisMetadata) {
        // 1. AI의 응답을 AnalysisResult 클래스로 파싱하도록 설정
        BeanOutputConverter<AnalysisResult> outputParser = new BeanOutputConverter<>(
            AnalysisResult.class);

        // 2. Metadata에서 기존 카테고리/태그 목록 추출 및 프롬프트용 문자열로 변환
        List<String> existingCategories = analysisMetadata.getCategoryList();
        List<String> existingTags = analysisMetadata.getTagList();

        String categoriesForPrompt =
            existingCategories.isEmpty() ? "없음" : String.join(", ", existingCategories);
        String tagsForPrompt =
            existingTags.isEmpty() ? "없음" : String.join(", ", existingTags);

        // 3. 시스템 프롬프트에 최종 출력 포맷 및 기존 데이터 정보를 주입
        PromptTemplate systemPromptTemplate = new PromptTemplate(SYSTEM_PROMPT);
        Map<String, Object> systemPromptContext = Map.of(
            "format", outputParser.getFormat(),
            "existing_categories", categoriesForPrompt,
            "existing_tags", tagsForPrompt
        );
        SystemMessage systemMessage = new SystemMessage(
            systemPromptTemplate.render(systemPromptContext));

        // 4. 사용자 프롬프트 생성
        PromptTemplate userPromptTemplate = new PromptTemplate(USER_PROMPT);
        UserMessage userMessage = new UserMessage(
            userPromptTemplate.render(Map.of("payload", payload)));

        // 5. 시스템/사용자 프롬프트를 합쳐 최종 프롬프트 생성
        Prompt finalPrompt = new Prompt(List.of(systemMessage, userMessage));

        log.info("[OpenAI] 프롬프트 분석을 요청합니다.");

        // 6. AI 호출 및 결과 파싱
        String responseContent = chatClient.prompt(finalPrompt).call()
            .content();

        assert responseContent != null;

        AnalysisResult analysisResult = outputParser.convert(responseContent);

        log.info("[OpenAI] 프롬프트 분석 완료. Category: {}(부모 Category: {}), Tags: {}",
            analysisResult.category(),
            analysisResult.parentCategory(),
            analysisResult.tagList());

        return analysisResult;
    }
}