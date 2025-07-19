package youngsu5582.tool.ai_tracker.api.dto;

import java.math.BigDecimal;

public record UsageStats(
    long totalRequests,
    long totalTokens,
    BigDecimal estimatedCostUsd,
    String currency,
    String lastUpdated
) {
}