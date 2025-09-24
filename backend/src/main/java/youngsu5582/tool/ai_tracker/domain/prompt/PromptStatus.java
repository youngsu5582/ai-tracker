package youngsu5582.tool.ai_tracker.domain.prompt;

public enum PromptStatus {
    /**
     * Prompt 를 단순히 저장한 상태
     */
    RECEIVED,
    /**
     * Prompt 를 분석중인 상태
     */
    ANALYZING,
    /**
     * 분석을 완료한 상태
     */
    COMPLETED,
    /**
     * 분석을 실패한 상태
     */
    FAILED
}
