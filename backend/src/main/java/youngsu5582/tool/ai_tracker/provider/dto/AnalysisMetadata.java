package youngsu5582.tool.ai_tracker.provider.dto;

import java.util.List;
import java.util.Map;

/**
 * 더 유의미한 분석을 하기 위해 제공되는 속성
 */
public record AnalysisMetadata(
    Map<AnalysisMetadataAttribute, List<String>> data
) {

    public enum AnalysisMetadataAttribute {
        CATEGORY_LIST,
        TAG_LIST
    }

    public List<String> getCategoryList() {
        return data.getOrDefault(AnalysisMetadataAttribute.CATEGORY_LIST, List.of());
    }

    public List<String> getTagList() {
        return data.getOrDefault(AnalysisMetadataAttribute.TAG_LIST, List.of());
    }
}
