package youngsu5582.tool.ai_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import youngsu5582.tool.ai_tracker.domain.Language;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;

@Slf4j
@Service
public class OpenAiService {

    private final WebClient chatWebClient;
    private final Map<Language, String> systemPrompts;
    private final Map<Language, String> evaluationSystemPrompts;
    private final Map<Language, String> keywordExtractionSystemPrompts;
    private final PromptRepository promptRepository;

    public OpenAiService(WebClient.Builder webClientBuilder,
        @Value("${openai.api.key}") String apiKey, PromptRepository promptRepository) {
        log.debug("Initializing OpenAiService with API Key: {}", apiKey.substring(0, 16));
        this.chatWebClient = webClientBuilder
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
        this.promptRepository = promptRepository;

        this.systemPrompts = Map.of(
            Language.EN, """
                You are an expert classifier. Analyze the user's prompt and classify it into one or, at most, two of the following categories:
                Software Development, Data Science, Creative Writing, Business & Marketing, Education & Learning, Daily Life & Conversation, Content Creation, Technical Q&A, Other.
                
                IMPORTANT RULES:
                - You MUST choose from the list provided.
                - Respond with ONLY the category name(s).
                - If you choose two categories, separate them with a comma.
                - Do NOT add any other words, explanations, or conversational text.
                
                Example 1: Software Development
                Example 2: Daily Life & Conversation
                Example 3: Creative Writing, Content Creation
                """,
            Language.KO, """
                당신은 전문가 분류기입니다. 사용자의 프롬프트를 분석하여 다음 카테고리 중 하나 또는 최대 두 개로 분류하세요:
                소프트웨어 개발, 데이터 과학, 창의적 글쓰기, 비즈니스 및 마케팅, 교육 및 학습, 일상 및 대화, 콘텐츠 제작, 기술 Q&A, 기타.
                
                중요 규칙:
                - 반드시 제공된 목록에서 선택해야 합니다.
                - 카테고리 이름으로만 응답해야 합니다.
                - 두 개의 카테고리를 선택하는 경우 쉼표로 구분하세요.
                - 다른 단어, 설명 또는 대화체 텍스트를 추가하지 마세요.
                
                예시 1: 소프트웨어 개발
                예시 2: 일상 및 대화
                예시 3: 창의적 글쓰기, 콘텐츠 제작
                """
        );

        this.evaluationSystemPrompts = Map.of(
            Language.EN, """
                You are an expert prompt evaluator. Evaluate the user's prompt based on its clarity, specificity, and potential for generating a useful AI response.
                If there is a previous conversation, consider it as context.
                Assign a score from 0 to 10, where 0 is a meaningless or unhelpful prompt, and 10 is an exceptionally clear and effective prompt.
                If the prompt is too short, vague, or a simple command like 'Translate this to Korean' without context, or 'Why is this not working?', assign a score of 0.
                Provide a list of reasons for your evaluation.
                
                Respond ONLY in JSON format with the following structure. DO NOT include any other text or explanation outside the JSON.
                {
                  "score": <integer, 0-10>,
                  "reasons": ["reason 1", "reason 2", ...]
                }
                
                Example:
                {
                  "score": 8,
                  "reasons": ["Clear intent", "Specific request", "Well-structured"]
                }
                """,
            Language.KO, """
                당신은 전문가 프롬프트 평가자입니다. 사용자의 프롬프트를 명확성, 구체성, 그리고 유용한 AI 응답을 생성할 잠재력을 기준으로 평가하세요.
                이전 대화가 있다면, 맥락으로 참고하세요.
                0점에서 10점 사이의 점수를 부여하세요. 0점은 의미 없거나 도움이 되지 않는 프롬프트이고, 10점은 매우 명확하고 효과적인 프롬프트입니다.
                프롬프트가 너무 짧거나, 모호하거나, '이것을 한국어로 번역해줘'와 같이 맥락 없는 간단한 명령이거나, '이거 왜 안돼?'와 같은 경우 0점을 부여하세요.
                평가에 대한 이유 목록을 제공하세요.
                
                오직 다음 JSON 형식으로만 응답하세요. JSON 외의 다른 텍스트나 설명을 포함하지 마세요.
                {
                  "score": <정수, 0-10>,
                  "reasons": ["이유 1", "이유 2", ...]
                }
                
                예시:
                {
                  "score": 8,
                  "reasons": ["명확한 의도", "구체적인 요청", "잘 구성됨"]
                }
                """
        );

        this.keywordExtractionSystemPrompts = Map.of(
            Language.EN, """
                You are an expert keyword and tag extractor. From the provided conversation text (which includes both the user's prompt and the AI's response), extract up to 15 highly relevant keywords and tags. These should include both single words and multi-word phrases that are crucial for understanding, categorizing, and searching the content. Prioritize terms that are:
                - Specific and descriptive.
                - Commonly used in the domain of the text.
                - Useful for both broad and narrow searches.
                - Suitable for display as clickable tags in a UI.

                Respond ONLY in JSON format with the following structure. DO NOT include any other text, explanations, or conversational elements. Ensure each keyword/tag is concise.
                {
                  "mainKeyword": "single_most_important_keyword",
                  "promptKeywords": ["keyword1", "keyword2"],
                  "promptTags": ["tag1", "tag2"],
                  "responseKeywords": ["keyword3", "keyword4"],
                  "responseTags": ["tag3", "tag4"]
                }
                """,
            Language.KO, """
                당신은 전문가 키워드 및 태그 추출기입니다. 제공된 대화 텍스트(사용자의 프롬프트와 AI의 응답을 모두 포함)에서 콘텐츠를 이해하고 분류하며 검색하는 데 중요한 단일 단어 및 다중 단어 구문을 포함하여 최대 15개의 매우 관련성 높은 키워드와 태그를 추출하세요. 다음을 우선적으로 고려하세요:
                - 구체적이고 설명적인 용어.
                - 텍스트 도메인에서 일반적으로 사용되는 용어.
                - 광범위한 검색과 세부적인 검색 모두에 유용한 용어.
                - UI에서 클릭 가능한 태그로 표시하기에 적합한 용어.

                오직 다음 JSON 형식으로만 응답하세요. JSON 외의 다른 텍스트나 설명을 포함하지 마세요.
                {
                  "mainKeyword": "가장_중요한_단일_키워드",
                  "promptKeywords": ["이벤트 소싱", "스냅샷"],
                  "promptTags": ["이벤트-소싱", "스냅샷", "카프카", "아웃박스-패턴"],
                  "responseKeywords": ["성능 최적화", "상태 복구"],
                  "responseTags": ["성능", "복구", "이벤트-저장소"]
                }
                """
        );
    }

    public Mono<ExtractedKeywordsResult> extractKeywords(String promptText, String responseText,
        Language language) {
        String systemPrompt = keywordExtractionSystemPrompts.getOrDefault(language,
            keywordExtractionSystemPrompts.get(Language.EN));

        String userMessageContent = String.format(
            "Prompt: %s\n\nResponse: %s",
            promptText,
            responseText != null && !responseText.isEmpty() ? responseText
                : "[No response provided]"
        );

        ChatRequest request = new ChatRequest("gpt-3.5-turbo",
            List.of(new ChatMessage("system", systemPrompt),
                new ChatMessage("user", userMessageContent)));

        return chatWebClient.post()
            .body(Mono.just(request), ChatRequest.class)
            .retrieve()
            .bodyToMono(String.class) // Read as String first to parse JSON manually
            .map(json -> {
                log.info("OpenAI Keyword Extraction Raw Response: {}", json);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootAll = mapper.readTree(json);
                    String content = rootAll
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                    // Parse the content string (which should be JSON) into ExtractedKeywordsResult
                    return mapper.readValue(content, ExtractedKeywordsResult.class);
                } catch (Exception e) {
                    log.error("Failed to parse OpenAI keyword extraction response", e);
                    return new ExtractedKeywordsResult("", List.of(), List.of(), List.of(),
                        List.of()); // Return empty on error
                }
            });
    }

    public Mono<PromptEvaluation> evaluatePrompt(String prompt, Language language,
        String conversationId) {
        String systemPrompt = evaluationSystemPrompts.getOrDefault(language,
            evaluationSystemPrompts.get(Language.EN));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));

        if (conversationId != null && !conversationId.isEmpty()) {
            List<Prompt> previousPrompts = promptRepository.findByConversationId(conversationId);
            previousPrompts.stream()
                .sorted(Comparator.comparing(Prompt::getTimestamp).reversed())
                .findFirst()
                .ifPresent(lastPrompt -> {
                    messages.add(new ChatMessage("user", lastPrompt.getPrompt()));
                    messages.add(new ChatMessage("assistant", lastPrompt.getResponse()));
                });
        }

        messages.add(new ChatMessage("user", prompt));

        ChatRequest request = new ChatRequest("gpt-3.5-turbo", messages);

        return chatWebClient.post()
            .body(Mono.just(request), ChatRequest.class)
            .retrieve()
            .bodyToMono(String.class) // Read as String first to parse JSON manually
            .map(json -> {
                log.info("OpenAI Evaluation Raw Response: {}", json);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    // 1) 전체 응답을 파싱
                    JsonNode rootAll = mapper.readTree(json);
                    // 2) choices[0].message.content 에 담긴 JSON 문자열을 꺼내기
                    String content = rootAll
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                    // 3) content 문자열(JSON)을 파싱
                    JsonNode root = mapper.readTree(content);
                    int score = root.path("score").asInt();
                    List<String> reasons = new ArrayList<>();
                    root.path("reasons").forEach(node -> reasons.add(node.asText()));
                    log.info("Parsed Evaluation: score={}, reasons={}", score, reasons);

                    return new PromptEvaluation(score, reasons);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse OpenAI evaluation response", e);
                }
            });
    }

    public Mono<String> categorizePrompt(String prompt, Language language, String conversationId) {
        String systemPrompt = systemPrompts.getOrDefault(language, systemPrompts.get(Language.EN));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));

        if (conversationId != null && !conversationId.isEmpty()) {
            List<Prompt> previousPrompts = promptRepository.findByConversationId(conversationId);
            previousPrompts.stream()
                .sorted(Comparator.comparing(Prompt::getTimestamp).reversed())
                .findFirst()
                .ifPresent(lastPrompt -> {
                    messages.add(new ChatMessage("user", lastPrompt.getPrompt()));
                    messages.add(new ChatMessage("assistant", lastPrompt.getResponse()));
                });
        }

        messages.add(new ChatMessage("user", prompt));

        ChatRequest request = new ChatRequest("gpt-3.5-turbo", messages);

        return chatWebClient.post()
            .body(Mono.just(request), ChatRequest.class)
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .map(response -> response.choices().get(0).message().content());
    }

    private record ChatRequest(String model, List<ChatMessage> messages) {

    }

    private record ChatMessage(String role, String content) {

    }

    private record ChatResponse(List<Choice> choices) {

    }

    private record Choice(ChatMessage message) {

    }

    public record PromptEvaluation(Integer score, List<String> reasons) {

    }
}