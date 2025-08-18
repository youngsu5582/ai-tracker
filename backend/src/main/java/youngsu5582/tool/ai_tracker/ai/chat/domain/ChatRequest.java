package youngsu5582.tool.ai_tracker.ai.chat.domain;

import java.util.List;

public record ChatRequest(String model, List<ChatMessage> messages) {

}
