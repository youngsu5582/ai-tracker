package youngsu5582.tool.ai_tracker.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.event.PromptEvaluationEvent;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.ai.chat.service.OpenAiService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromptEvaluationListener {

    private final PromptRepository promptRepository;  // JpaRepository<String, Prompt> 등 동기 리포지토리
    private final OpenAiService openAiService;

    @EventListener
    @Async  // Spring TaskExecutor 위에서 비동기로 실행
    public void handlePromptEvaluationEvent(PromptEvaluationEvent event) {
        log.info("Handling PromptEvaluationEvent for promptId: {}", event.promptId());

        promptRepository.findById(event.promptId()).ifPresent(prompt -> openAiService.evaluatePrompt(event.promptText(), event.language(), prompt.getConversationId())
            .flatMap(evaluation -> {
                log.info("Prompt evaluation result: {}", evaluation);
                boolean isMeaningless = evaluation.score() <= 0;

                return Mono.fromCallable(() -> {
                        Prompt updated = new Prompt(
                            prompt.getId(),
                            prompt.getConversationId(),
                            prompt.getPrompt(),
                            prompt.getResponse(),
                            prompt.getModel(),
                            prompt.getSource(),
                            prompt.getTimestamp(),
                            prompt.getCategory(),
                            evaluation.score(),
                            evaluation.reasons(),
                            isMeaningless,
                            prompt.getLanguage(),
                            prompt.getMainKeyword() // Use getMainKeyword
                        );
                        return promptRepository.save(updated);
                    })
                    .subscribeOn(Schedulers.boundedElastic());
            })
            .doOnSuccess(savedPrompt ->
                log.info("Prompt updated with evaluation: {}", savedPrompt)
            )
            .doOnError(e ->
                log.error("Error processing prompt evaluation for {}: {}", event.promptId(),
                    e.getMessage(), e)
            )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                updatedPrompt ->
                    log.info("Prompt evaluation and update completed for promptId: {}",
                        event.promptId()),
                e ->
                    log.error("Final error in prompt evaluation chain for {}: {}", event.promptId(),
                        e.getMessage(), e),
                () ->
                    log.info("Prompt evaluation chain for {} completed successfully.",
                        event.promptId())
            ));
    }
}
