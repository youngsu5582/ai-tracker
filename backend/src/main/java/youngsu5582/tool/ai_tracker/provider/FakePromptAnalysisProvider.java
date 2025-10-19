package youngsu5582.tool.ai_tracker.provider;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisResult;

@Slf4j
@Component
@ConditionalOnMissingBean(PromptAnalysisProvider.class)
public class FakePromptAnalysisProvider implements PromptAnalysisProvider {

    @PostConstruct
    public void init() {
        log.info("확인용 FAKE 프롬프트 분석이 활성화되었습니다.");
    }

    @Override
    public AnalysisResult analyze(String payload, AnalysisMetadata analysisMetadata) {
        log.warn("[FAKE] 프롬프트 분석을 진행합니다. OpenAI API Key가 설정되지 않았습니다.");
        return AnalysisResult
            .builder()
            .category("Uncategorized")
            .parentCategory("None")
            .tagList(List.of("fake-analysis"))
            .build();
    }
}
