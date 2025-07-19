package youngsu5582.tool.ai_tracker.event;

import org.springframework.context.ApplicationEvent;
import youngsu5582.tool.ai_tracker.domain.Language;

public class PromptEvaluationEvent extends ApplicationEvent {
    private final String promptId;
    private final String promptText;
    private final Language language;

    public PromptEvaluationEvent(Object source, String promptId, String promptText, Language language) {
        super(source);
        this.promptId = promptId;
        this.promptText = promptText;
        this.language = language;
    }

    public String getPromptId() {
        return promptId;
    }

    public String getPromptText() {
        return promptText;
    }

    public Language getLanguage() {
        return language;
    }
}
