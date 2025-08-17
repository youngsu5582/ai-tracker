package youngsu5582.tool.ai_tracker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "prompts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Prompt {

    @Id
    private String id;
    private String conversationId;
    private String prompt;
    private String response;
    private String model;
    private String source;
    private Instant timestamp;
    private String category;
    private Integer score;
    private List<String> evaluationReasons;
    private Boolean isMeaningless;
    private Language language;
    private String mainKeyword;
}
