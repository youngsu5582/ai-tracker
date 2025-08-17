package youngsu5582.tool.ai_tracker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "tag_indexes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagIndex {
    @Id
    private String keyword;
    private Set<String> promptIds;
}