package youngsu5582.tool.ai_tracker.ai.chat.domain;

import java.util.Objects;

public record ChatMessage(ChatMessageRole role, String content) {

    public ChatMessage {
        Objects.requireNonNull(content,"content is not empty");
        Objects.requireNonNull(role,"role is not empty");
    }
}
