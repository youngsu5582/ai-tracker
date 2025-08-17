package youngsu5582.tool.ai_tracker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prompt_keywords")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptKeyword {
    @Id
    private String id; // Same as promptId
    private String keyword;
}