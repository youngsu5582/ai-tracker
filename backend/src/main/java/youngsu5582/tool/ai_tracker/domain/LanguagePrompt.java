package youngsu5582.tool.ai_tracker.domain;

import java.util.Objects;

public record LanguagePrompt(
    String prompt,
    Language language
) {

    public LanguagePrompt {
        Objects.requireNonNull(prompt, "prompt must not be null");
        Objects.requireNonNull(language, "language must not be null");
    }
}
