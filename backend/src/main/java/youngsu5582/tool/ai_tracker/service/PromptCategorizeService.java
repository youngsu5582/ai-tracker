package youngsu5582.tool.ai_tracker.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import youngsu5582.tool.ai_tracker.ai.chat.openai.OpenAiService;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.domain.PromptBundle;
import youngsu5582.tool.ai_tracker.event.PromptCreatedEvent;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptCategorizeService {

    private final PromptRepository promptRepository;
    private final OpenAiService openAiService;

    @Async
    @EventListener
    public void categorizePrompt(PromptCreatedEvent event) {
        Prompt prompt = promptRepository.findById(event.id())
            .orElseThrow(() -> new NoSuchElementException("prompt not found. ID: " + event.id()));

        PromptBundle bundle = new PromptBundle(
            promptRepository.findByConversationId(prompt.getConversationId()));
        Prompt lastPrompt = bundle.getLatestPrompt();
        openAiService.categorizePrompt(prompt.languagePrompt(), lastPrompt.conversation())
            .doOnNext(category -> log.info("Category: {}", category))
            .subscribe(
                category -> {
                    prompt.updateCategory(category);
                    promptRepository.save(prompt);
                });
    }
}
