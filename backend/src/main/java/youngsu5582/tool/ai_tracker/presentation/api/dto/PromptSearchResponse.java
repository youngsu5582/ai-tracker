package youngsu5582.tool.ai_tracker.presentation.api.dto;

import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult.PromptSummary;

import java.time.Instant;
import java.util.List;

public record PromptSearchResponse(
    long total,
    List<PromptSummaryResponse> prompts
) {

    public static PromptSearchResponse from(PromptSearchResult result) {
        return new PromptSearchResponse(
            result.total(),
            result.prompts().stream()
                .map(PromptSummaryResponse::from)
                .toList()
        );
    }

    public record PromptSummaryResponse(
        String id,
        Long promptId,
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

        public static PromptSummaryResponse from(PromptSummary summary) {
            return new PromptSummaryResponse(
                summary.promptUuid(),
                summary.id(),
                summary.messageId(),
                summary.status(),
                summary.provider(),
                summary.category(),
                summary.parentCategory(),
                summary.tags(),
                summary.createdAt(),
                summary.analyzedAt(),
                summary.payload(),
                summary.error()
            );
        }
    }
}
