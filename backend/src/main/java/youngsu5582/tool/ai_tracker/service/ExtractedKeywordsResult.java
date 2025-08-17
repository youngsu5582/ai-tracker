package youngsu5582.tool.ai_tracker.service;

import java.util.List;

public record ExtractedKeywordsResult(
    String mainKeyword,
    List<String> promptKeywords,
    List<String> promptTags,
    List<String> responseKeywords,
    List<String> responseTags
) {

}