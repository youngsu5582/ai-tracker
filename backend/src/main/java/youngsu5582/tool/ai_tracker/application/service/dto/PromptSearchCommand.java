package youngsu5582.tool.ai_tracker.application.service.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record PromptSearchCommand(
        String searchText,
        Instant from,
        Instant to
) {

    private static final int DEFAULT_SEARCH_DAYS = 14;


    public PromptSearchCommand {
        Instant effectiveTo = to == null ? Instant.now() : to;
        Instant effectiveFrom = from == null
                ? effectiveTo.minus(DEFAULT_SEARCH_DAYS, ChronoUnit.DAYS)
                : from;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException(
                    "'from(%s)' must not be after 'to(%s)'".formatted(effectiveFrom, effectiveTo)
            );
        }

        from = effectiveFrom;
        to = effectiveTo;
    }

}
