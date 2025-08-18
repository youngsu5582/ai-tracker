package youngsu5582.tool.ai_tracker.ai.chat.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import youngsu5582.tool.ai_tracker.ai.chat.domain.ChatMessage;
import youngsu5582.tool.ai_tracker.ai.chat.domain.ChatMessageRole;
import youngsu5582.tool.ai_tracker.ai.chat.domain.ChatMessages;
import youngsu5582.tool.ai_tracker.ai.chat.domain.ChatRequest;
import youngsu5582.tool.ai_tracker.ai.chat.domain.ChatResponse;
import youngsu5582.tool.ai_tracker.config.PromptConfig;
import youngsu5582.tool.ai_tracker.domain.Language;
import youngsu5582.tool.ai_tracker.domain.LanguagePrompt;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.domain.PromptConversation;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.service.ExtractedKeywordsResult;

@Slf4j
@Service
public class OpenAiService {


    private final WebClient chatWebClient;
    private final String model;

    private final Map<Language, String> categoryExtraction;
    private final Map<Language, String> evaluationSystemPrompts;
    private final Map<Language, String> keywordExtractionSystemPrompts;
    private final PromptRepository promptRepository;

    public OpenAiService(WebClient.Builder webClientBuilder,
        @Value("${openai.api.key}") String apiKey,
        @Value("${openai.api.model}") String model,
        PromptRepository promptRepository,
        PromptConfig promptConfig) {
        log.info("Initializing OpenAiService [model: {}]", model);
        this.chatWebClient = webClientBuilder
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
        this.model = model;
        this.promptRepository = promptRepository;

        this.categoryExtraction = promptConfig.categoryExtraction();
        this.evaluationSystemPrompts = promptConfig.evaluationSystemPrompts();
        this.keywordExtractionSystemPrompts = promptConfig.keywordExtractionSystemPrompts();
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

        ChatRequest request = new ChatRequest(model,
            List.of(new ChatMessage(ChatMessageRole.SYSTEM, systemPrompt),
                new ChatMessage(ChatMessageRole.USER, userMessageContent)));

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
                    log.warn(
                        "Failed to parse OpenAI keyword extraction response, continuing with empty value",
                        e);
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
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM, systemPrompt));

        if (conversationId != null && !conversationId.isEmpty()) {
            List<Prompt> previousPrompts = promptRepository.findByConversationId(conversationId);
            previousPrompts.stream()
                .sorted(Comparator.comparing(Prompt::getTimestamp).reversed())
                .findFirst()
                .ifPresent(lastPrompt -> {
                    messages.add(new ChatMessage(ChatMessageRole.USER, lastPrompt.getPrompt()));
                    messages.add(
                        new ChatMessage(ChatMessageRole.ASSISTANT, lastPrompt.getResponse()));
                });
        }

        messages.add(new ChatMessage(ChatMessageRole.USER, prompt));

        ChatRequest request = new ChatRequest(model, messages);

        return chatWebClient.post()
            .body(Mono.just(request), ChatRequest.class)
            .retrieve()
            .bodyToMono(String.class) // Read as String first to parse JSON manually
            .handle((json, sink) -> {
                log.info("OpenAI Evaluation Raw Response: {}", json);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    // 1) parse all response
                    JsonNode rootAll = mapper.readTree(json);
                    // 2) put out json value in choices[0].message.content
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

                    sink.next(new PromptEvaluation(score, reasons));
                } catch (Exception e) {
                    sink.error(
                        new OpenAiErrorException("Failed to parse OpenAI evaluation response", e));
                }
            });
    }

    public Mono<String> categorizePrompt(LanguagePrompt languagePrompt,
        PromptConversation lastConversation) {
        String systemPrompt = categoryExtraction.getOrDefault(languagePrompt.language(),
            categoryExtraction.get(Language.EN));

        ChatMessages messages = ChatMessages.empty();

        messages.add(ChatMessageRole.SYSTEM, systemPrompt);
        messages.add(ChatMessageRole.USER, lastConversation.promptRequest());
        messages.add(ChatMessageRole.ASSISTANT, lastConversation.promptResponse());
        messages.add(ChatMessageRole.USER, languagePrompt.prompt());

        ChatRequest request = new ChatRequest(model, messages.toList());

        return chatWebClient.post()
            .body(Mono.just(request), ChatRequest.class)
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .map(ChatResponse::getContent)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(10)));
    }

    public record PromptEvaluation(Integer score, List<String> reasons) {

    }
}