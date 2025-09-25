package youngsu5582.tool.ai_tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import youngsu5582.tool.ai_tracker.domain.category.Category;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptStatus;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;

public class MockEntityFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Prompt createPrompt(PromptStatus promptStatus) {
        return Prompt.builder()
            .status(promptStatus)
            .payload(handleException(() -> objectMapper.writeValueAsString("value")))
            .build();
    }

    public static Tag createTag(String name) {
        return Tag.builder()
            .name(name)
            .build();
    }

    public static Category createCategory(String name) {
        return Category.builder()
            .name(name)
            .build();
    }

    public static Category createCategory(String name, Category parent) {
        return Category.builder()
            .name(name)
            .parentCategory(parent)
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
