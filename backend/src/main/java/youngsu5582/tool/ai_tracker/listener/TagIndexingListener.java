package youngsu5582.tool.ai_tracker.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import youngsu5582.tool.ai_tracker.ai.chat.openai.OpenAiService;
import youngsu5582.tool.ai_tracker.event.PromptEvaluationEvent;
import youngsu5582.tool.ai_tracker.repository.TagRepository;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.domain.TagIndex;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TagIndexingListener {

    private final PromptRepository promptRepository;
    private final OpenAiService openAiService;
    private final TagRepository tagRepository;

    @EventListener
    @Async
    public void handlePromptIndexingEvent(PromptEvaluationEvent event) {
        log.info("Handling KeywordIndexingEvent for promptId: {}", event.promptId());

        promptRepository.findById(event.promptId())
            .ifPresent(prompt -> openAiService.extractKeywords(prompt.getPrompt(), prompt.getResponse(), prompt.getLanguage())
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(extracted -> {
                    log.info("Extracted keywords for prompt {}: {}", prompt.getId(), extracted);
                    prompt.setMainKeyword(extracted.mainKeyword());
                    promptRepository.save(prompt);

                    // Index tags for search
                    extracted.promptTags().forEach(tag -> updateTagIndex(tag, prompt.getId()));
                    extracted.responseTags().forEach(tag -> updateTagIndex(tag, prompt.getId()));
                }, e -> log.error("Error extracting keywords for prompt {}: {}", prompt.getId(), e.getMessage(), e)));
    }

    private void updateTagIndex(String tag, String promptId) {
        tagRepository.findById(tag)
            .ifPresentOrElse(
                existingIndex -> {
                    existingIndex.getPromptIds().add(promptId);
                    tagRepository.save(existingIndex);
                    log.debug("Updated tag index for '{}': added prompt {}", tag, promptId);
                },
                () -> {
                    Set<String> promptIds = new HashSet<>();
                    promptIds.add(promptId);
                    TagIndex newIndex = new TagIndex(tag, promptIds);
                    tagRepository.save(newIndex);
                    log.debug("Created new tag index for '{}': added prompt {}", tag, promptId);
                }
            );
    }
}