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
import youngsu5582.tool.ai_tracker.service.OpenAiService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromptEvaluationListener {

    private final PromptRepository promptRepository;  // JpaRepository<String, Prompt> 등 동기 리포지토리
    private final OpenAiService openAiService;

    @EventListener
    @Async  // Spring TaskExecutor 위에서 비동기로 실행
    public void handlePromptEvaluationEvent(PromptEvaluationEvent event) {
        log.info("Handling PromptEvaluationEvent for promptId: {}", event.getPromptId());

        openAiService.categorizePrompt(event.getPromptText(), event.getLanguage())
            .zipWith(openAiService.evaluatePrompt(event.getPromptText(), event.getLanguage()))
            .flatMap(tuple -> {
                log.info("Prompt evaluation result: {}", tuple);
                String category = tuple.getT1();
                OpenAiService.PromptEvaluation evaluation = tuple.getT2();
                boolean isMeaningless = evaluation.score() <= 0;

                // Optional<Prompt> → Mono<Prompt>
                return Mono.fromCallable(() -> promptRepository.findById(event.getPromptId()).orElse(null))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(existingPrompt -> {
                        if (existingPrompt == null) {
                            log.warn("Prompt with ID {} not found for update.", event.getPromptId());
                            return Mono.empty();
                        }
                        // save(...)는 블로킹이므로 boundedElastic 스케줄러에서 실행
                        return Mono.fromCallable(() -> {
                                Prompt updated = new Prompt(
                                    existingPrompt.id(),
                                    existingPrompt.prompt(),
                                    existingPrompt.model(),
                                    existingPrompt.source(),
                                    existingPrompt.timestamp(),
                                    category,
                                    evaluation.score(),
                                    evaluation.reasons(),
                                    isMeaningless,
                                    existingPrompt.language()
                                );
                                return promptRepository.save(updated);
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                    })
                    .doOnSuccess(savedPrompt ->
                        log.info("Prompt updated with evaluation: {}", savedPrompt)
                    );
            })
            .doOnError(e ->
                log.error("Error processing prompt evaluation for {}: {}", event.getPromptId(), e.getMessage(), e)
            )
            // openAiService 호출 자체는 논블로킹이므로, 굳이 전체 체인에 subscribeOn을 걸 필요는 없지만
            // 혹시 내부에 블로킹이 더 있다면 이 라인으로도 잡을 수 있습니다.
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                updatedPrompt ->
                    log.info("Prompt evaluation and update completed for promptId: {}", event.getPromptId()),
                e ->
                    log.error("Final error in prompt evaluation chain for {}: {}", event.getPromptId(), e.getMessage(), e),
                () ->
                    log.info("Prompt evaluation chain for {} completed successfully.", event.getPromptId())
            );
    }
}
