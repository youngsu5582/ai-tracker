package youngsu5582.tool.ai_tracker.domain;

import java.util.Comparator;
import java.util.List;

public record PromptBundle(
    List<Prompt> promptList
) {

    public PromptBundle {
        promptList = promptList == null ? List.of() : List.copyOf(promptList);
    }

    public Prompt getLatestPrompt() {
        return promptList.stream().max(Comparator.comparing(Prompt::getTimestamp)).orElse(null);
    }
}
