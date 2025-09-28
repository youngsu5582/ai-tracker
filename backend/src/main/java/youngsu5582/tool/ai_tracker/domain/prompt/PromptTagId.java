package youngsu5582.tool.ai_tracker.domain.prompt;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class PromptTagId implements Serializable {

    private Long prompt;
    private Long tag;

}
