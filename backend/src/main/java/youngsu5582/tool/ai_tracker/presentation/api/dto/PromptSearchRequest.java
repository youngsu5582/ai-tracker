package youngsu5582.tool.ai_tracker.presentation.api.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchCommand;

import java.time.Instant;

public record PromptSearchRequest(
        String searchText,
        @DateTimeFormat(iso = ISO.DATE_TIME) Instant from,
        @DateTimeFormat(iso = ISO.DATE_TIME) Instant to
) {

    public PromptSearchCommand toCommand() {
        return new PromptSearchCommand(
                searchText,
                from,
                to
        );
    }
}
