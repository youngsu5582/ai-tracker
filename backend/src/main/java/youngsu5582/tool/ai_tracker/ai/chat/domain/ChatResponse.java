package youngsu5582.tool.ai_tracker.ai.chat.domain;

import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public record ChatResponse(List<Choice> choices) {

    public String getContent() {
        // Empty array consider request fail
        if (CollectionUtils.isEmpty(choices)) {
            throw new IllegalArgumentException(
                "Received an empty or invalid response from OpenAI.");
        }
        Choice choice = choices.getFirst();
        if (!StringUtils.hasText(choice.getMessageContent())) {
            throw new IllegalStateException(
                "Received an empty or invalid response from OpenAI.");
        }
        return choice.getMessageContent();
    }
}
