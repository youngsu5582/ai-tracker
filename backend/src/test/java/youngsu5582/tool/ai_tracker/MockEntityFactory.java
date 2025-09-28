package youngsu5582.tool.ai_tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptStatus;

public class MockEntityFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Prompt createPrompt(PromptStatus promptStatus) {
        return Prompt.builder()
            .status(promptStatus)
            .payload(handleException(() -> objectMapper.writeValueAsString("value")))
            .build();
    }

    private static <T> T handleException(ThrowableSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to handle exception", e);
        }
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {

        T get() throws Throwable;
    }
}
