package youngsu5582.tool.ai_tracker.api.dto;

import java.math.BigDecimal;

public record RemainingBalanceResponse(
    BigDecimal remainingAmount,
    String currency,
    String lastUpdated
) {
}