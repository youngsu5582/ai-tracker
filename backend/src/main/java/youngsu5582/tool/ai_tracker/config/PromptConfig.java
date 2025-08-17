package youngsu5582.tool.ai_tracker.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import youngsu5582.tool.ai_tracker.domain.Language;

@Slf4j
@Configuration
public class PromptConfig {

    private static final String CATEGORY_EXTRACTION_PATH = "prompts/system/category-extraction.json";
    private static final String EVALUATION_PROMPTS_PATH = "prompts/system/evaluation-prompts.json";
    private static final String KEYWORD_EXTRACTION_PROMPTS_PATH = "prompts/system/keyword-extraction-prompts.json";

    @Bean
    public Map<Language, String> categoryExtraction() {
        return loadPrompts(CATEGORY_EXTRACTION_PATH, "category extraction");
    }

    @Bean
    public Map<Language, String> evaluationSystemPrompts() {
        return loadPrompts(EVALUATION_PROMPTS_PATH, "evaluation system");
    }

    @Bean
    public Map<Language, String> keywordExtractionSystemPrompts() {
        return loadPrompts(KEYWORD_EXTRACTION_PROMPTS_PATH, "keyword extraction system");
    }

    private Map<Language, String> loadPrompts(String path, String promptType) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            Map<Language, String> prompts = objectMapper().readValue(inputStream,
                new TypeReference<>() {
                });
            log.info("Success to load {} prompts. path: {}, map: {}", promptType, path,
                prompts.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, value -> value.getValue().length())));
            return prompts;
        } catch (IOException e) {
            log.warn("Failed to load {} prompts. path: {} |", promptType, path, e);
            return Collections.emptyMap();
        }
    }

    @Bean
    protected ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
