package youngsu5582.tool.ai_tracker.service.search;

import youngsu5582.tool.ai_tracker.domain.Prompt;
import java.util.List;

public interface PromptSearchService {
    List<Prompt> search(String keyword);
    void indexPrompt(Prompt prompt);
    List<String> getAllMainKeywords();
    List<String> getAllTags();
}