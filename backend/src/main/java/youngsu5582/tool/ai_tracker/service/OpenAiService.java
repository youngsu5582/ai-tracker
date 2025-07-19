package youngsu5582.tool.ai_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import youngsu5582.tool.ai_tracker.domain.Language;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAiService {

    private final WebClient chatWebClient;
    private final Map<Language, String> systemPrompts;
    private final Map<Language, String> evaluationSystemPrompts;

    public OpenAiService(WebClient.Builder webClientBuilder,
        @Value("${openai.api.key}") String apiKey) {
        log.info("Initializing OpenAiService with API Key: {}", apiKey.substring(0, 16));
        this.chatWebClient = webClientBuilder
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();

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
    }

    public Mono<PromptEvaluation> evaluatePrompt(String prompt, Language language) {
        String systemPrompt = evaluationSystemPrompts.getOrDefault(language,
            evaluationSystemPrompts.get(Language.EN));
        ChatRequest request = new ChatRequest("gpt-3.5-turbo",
            List.of(new ChatMessage("system", systemPrompt), new ChatMessage("user", prompt)));

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

    public Mono<String> categorizePrompt(String prompt, Language language) {
        String systemPrompt = systemPrompts.getOrDefault(language, systemPrompts.get(Language.EN));
        ChatRequest request = new ChatRequest("gpt-3.5-turbo",
            List.of(new ChatMessage("system", systemPrompt), new ChatMessage("user", prompt)));

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