package youngsu5582.tool.ai_tracker.application.service.dto;

import java.time.Instant;

public record PromptSearchCommand(
        String searchText,
        Instant from,
        Instant to
) {

}
