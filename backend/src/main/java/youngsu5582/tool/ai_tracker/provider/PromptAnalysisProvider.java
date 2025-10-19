package youngsu5582.tool.ai_tracker.provider;

import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisResult;

/**
 * Prompt 를 분석해서 결과를 제공해주는 제공자
 */
public interface PromptAnalysisProvider {

    AnalysisResult analyze(String payload, AnalysisMetadata analysisMetadata);
}
