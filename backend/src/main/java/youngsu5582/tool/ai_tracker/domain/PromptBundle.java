package youngsu5582.tool.ai_tracker.domain;

import java.util.Comparator;
import java.util.List;

public record PromptBundle(
    List<Prompt> promptList
) {

    public Prompt getLastPrompt() {
        return promptList.stream().max(Comparator.comparing(Prompt::getTimestamp)).orElse(null);
    }
}
