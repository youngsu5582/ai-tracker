package youngsu5582.tool.ai_tracker.event;

import lombok.Getter;
import youngsu5582.tool.ai_tracker.domain.Language;

public record PromptEvaluationEvent(String promptId, String promptText, Language language, String conversationId) {

}
