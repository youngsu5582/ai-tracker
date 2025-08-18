package youngsu5582.tool.ai_tracker.ai.chat.domain;

import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public record ChatResponse(List<Choice> choices) {

    private static final String INVALID_RESPONSE_MSG = "Received an empty or invalid response";

    public String getContent() {
        // Empty array consider request fail
        if (CollectionUtils.isEmpty(choices)) {
            throw new IllegalArgumentException(
                INVALID_RESPONSE_MSG);
        }
        Choice choice = choices.getFirst();
        if (!StringUtils.hasText(choice.getMessageContent())) {
            throw new IllegalStateException(
                INVALID_RESPONSE_MSG);
        }
        return choice.getMessageContent();
    }
}
