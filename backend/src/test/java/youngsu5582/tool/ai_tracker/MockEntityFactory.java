package youngsu5582.tool.ai_tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import java.util.Collections;
import java.util.UUID;
import youngsu5582.tool.ai_tracker.domain.category.Category;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptStatus;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureRequest;

public class MockEntityFactory {

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.create();
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

    public static CaptureRequest getCaptureRequest() {
        return FIXTURE_MONKEY.giveMeBuilder(CaptureRequest.class)
            .setNotNull("id")
            .setNotNull("message")
            .setNotNull("message.content")
            .minSize("message.content.parts", 1)
            .set("message.author.metadata", Collections.emptyMap())
            .set("id", UUID.randomUUID().toString())
            .sample();
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {

        T get() throws Throwable;
    }
}
