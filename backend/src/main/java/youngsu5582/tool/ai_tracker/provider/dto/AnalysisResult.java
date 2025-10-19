package youngsu5582.tool.ai_tracker.provider.dto;

import java.util.List;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record AnalysisResult(
    String category,

    @Nullable String parentCategory,
    List<String> tagList
) {

}
