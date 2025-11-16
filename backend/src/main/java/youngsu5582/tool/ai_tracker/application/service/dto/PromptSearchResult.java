package youngsu5582.tool.ai_tracker.application.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

public record PromptSearchResult(
        long total,
        List<PromptSummary> prompts
) {

    @JsonIgnore
    public List<Long> getPromptIds() {
        return prompts.stream().map(PromptSummary::id).toList();
    }

    public PromptSearchResult {
        prompts = List.copyOf(prompts);
    }

    @Builder
    public record PromptSummary(
            String promptUuid,
            Long id,
            String messageId,
            String status,
            String provider,
            String category,
            String parentCategory,
            List<String> tags,
            Instant createdAt,
            Instant analyzedAt,
            String payload,
            String error
    ) {
    }
}
