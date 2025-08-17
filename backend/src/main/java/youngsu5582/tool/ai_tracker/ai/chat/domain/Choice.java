package youngsu5582.tool.ai_tracker.ai.chat.domain;

public record Choice(ChatMessage message) {

    public String getMessageContent() {
        if (message == null) {
            return "";
        }
        return message.content();
    }
}
