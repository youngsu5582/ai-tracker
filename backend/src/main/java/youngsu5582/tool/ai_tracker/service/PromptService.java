package youngsu5582.tool.ai_tracker.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import youngsu5582.tool.ai_tracker.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.event.PromptEvaluationEvent;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.service.search.PromptSearchService;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final OpenAiService openAiService;
    private final ApplicationEventPublisher eventPublisher;
    private final PromptSearchService promptSearchService;

    public void savePrompt(CaptureRequest request) {
        openAiService.categorizePrompt(request.prompt(), request.language(), request.conversationId())
            .flatMap(category -> {
                LocalDateTime timestamp = LocalDateTime.parse(request.timestamp(),
                    DateTimeFormatter.ISO_DATE_TIME);
                Prompt newPrompt = new Prompt(null, request.conversationId(), request.prompt(), request.response(), request.model(),
                    request.source(), timestamp.toInstant(ZoneOffset.UTC), category, null, null,
                    null, request.language(), null);

                return Mono.fromCallable(() -> promptRepository.save(newPrompt))
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess(savedPrompt -> {
                        // Publish event after prompt is successfully saved and has an ID
                        eventPublisher.publishEvent(
                            new PromptEvaluationEvent(savedPrompt.getId(), savedPrompt.getPrompt(),
                                savedPrompt.getLanguage(), savedPrompt.getConversationId()));
                    });
            })
            .doOnError(Throwable::printStackTrace)
            .subscribe();
    }

    public List<Prompt> getPromptsByConversationId(String conversationId) {
        return promptRepository.findByConversationId(conversationId);
    }

    public List<Prompt> searchPrompts(String keyword) {
        return promptSearchService.search(keyword);
    }

    public List<String> getAllMainKeywords() {
        return promptSearchService.getAllMainKeywords();
    }

    public List<String> getAllTags() {
        return promptSearchService.getAllTags();
    }
}