package youngsu5582.tool.ai_tracker.service;

import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import youngsu5582.tool.ai_tracker.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.domain.Language;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.event.PromptEvaluationEvent;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import youngsu5582.tool.ai_tracker.service.OpenAiService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final OpenAiService openAiService;
    private final ApplicationEventPublisher eventPublisher;

    public void savePrompt(CaptureRequest request) {
        openAiService.categorizePrompt(request.prompt(), request.language())
            .flatMap(category -> {
                LocalDateTime timestamp = LocalDateTime.parse(request.timestamp(),
                    DateTimeFormatter.ISO_DATE_TIME);
                // MongoDB will generate the ID, so we pass null for it.
                // Initialize score, reasons, isMeaningless to null/default values
                Prompt newPrompt = new Prompt(null, request.prompt(), request.model(),
                    request.source(), timestamp.toInstant(ZoneOffset.UTC), category, null, null,
                    null, request.language());
                return Mono.fromCallable(() -> promptRepository.save(newPrompt))
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess(savedPrompt -> {
                        // Publish event after prompt is successfully saved and has an ID
                        eventPublisher.publishEvent(
                            new PromptEvaluationEvent(this, savedPrompt.id(), savedPrompt.prompt(),
                                savedPrompt.language()));
                    });
            })
            .doOnError(Throwable::printStackTrace) // Basic error handling
            .subscribe();
    }
}
