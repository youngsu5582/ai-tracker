package youngsu5582.tool.ai_tracker.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prompts")
public record Prompt(
    @Id
    String id,
    String prompt,
    String model,
    String source,
    Instant timestamp,
    String category,
    Integer score,
    List<String> evaluationReasons,
    Boolean isMeaningless,
    Language language
) {
}
