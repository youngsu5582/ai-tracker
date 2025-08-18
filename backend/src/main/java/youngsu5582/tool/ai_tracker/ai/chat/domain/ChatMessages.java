package youngsu5582.tool.ai_tracker.ai.chat.domain;

import java.util.ArrayList;
import java.util.List;

public record ChatMessages(List<ChatMessage> messageList) {

    public static ChatMessages empty() {
        return new ChatMessages(new ArrayList<>());
    }

    public void add(ChatMessageRole role, String messageContent) {
        messageList.add(new ChatMessage(role, messageContent));
    }


    public List<ChatMessage> toList() {
        return messageList;
    }

}
