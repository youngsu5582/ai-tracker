package youngsu5582.tool.ai_tracker.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import youngsu5582.tool.ai_tracker.event.PromptEvaluationEvent;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.service.search.PromptSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromptIndexingListener {

    private final PromptRepository promptRepository;
    private final PromptSearchService promptSearchService;

    @EventListener
    @Async
    public void handlePromptIndexingEvent(PromptEvaluationEvent event) {
        log.info("Handling PromptIndexingEvent for promptId: {}", event.promptId());
        promptRepository.findById(event.promptId())
            .ifPresent(promptSearchService::indexPrompt);
    }
}