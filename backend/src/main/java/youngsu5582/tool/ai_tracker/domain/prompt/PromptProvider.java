package youngsu5582.tool.ai_tracker.domain.prompt;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PromptProvider {
    OPEN_AI("open-ai");

    @JsonValue
    private final String alias;

    PromptProvider(String alias) {
        this.alias = alias;
    }
}
