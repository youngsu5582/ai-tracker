package youngsu5582.tool.ai_tracker.ai.chat.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatMessageRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    ;

    @JsonValue
    private final String role;

    ChatMessageRole(String role) {
        this.role = role;
    }

}
