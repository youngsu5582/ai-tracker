package youngsu5582.tool.ai_tracker.api.dto;

import youngsu5582.tool.ai_tracker.domain.Language;

public record CaptureRequest(
    String conversationId,
    String prompt,
    String response,
    String model,
    String source,
    String timestamp, // Timestamp will be parsed in the service
    Language language
) {
    public Language language() {
        return language == null ? Language.KO : language;
    }
}
