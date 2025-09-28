package youngsu5582.tool.ai_tracker.domain.prompt;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;

@Entity
@IdClass(PromptTagId.class)
@Table(name = "prompt_tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class PromptTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;


    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public PromptTag(Prompt prompt, Tag tag) {
        this.prompt = Objects.requireNonNull(prompt, "prompt must not be null");
        this.tag = Objects.requireNonNull(tag, "tag must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PromptTag promptTag = (PromptTag) o;
        return Objects.equals(prompt, promptTag.prompt) && Objects.equals(tag, promptTag.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prompt, tag);
    }
}
