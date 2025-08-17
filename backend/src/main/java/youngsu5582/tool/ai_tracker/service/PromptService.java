package youngsu5582.tool.ai_tracker.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import youngsu5582.tool.ai_tracker.ai.chat.service.OpenAiService;
import youngsu5582.tool.ai_tracker.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.domain.PromptConverter;
import youngsu5582.tool.ai_tracker.event.PromptCreatedEvent;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.service.search.PromptSearchService;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final OpenAiService openAiService;
    private final ApplicationEventPublisher eventPublisher;
    private final PromptSearchService promptSearchService;
    private final PromptConverter promptConverter;

    public void savePrompt(CaptureRequest request) {
        var prompt = promptConverter.convert(request);
        var promptId = promptRepository.save(prompt).getId();
        eventPublisher.publishEvent(new PromptCreatedEvent(promptId));
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