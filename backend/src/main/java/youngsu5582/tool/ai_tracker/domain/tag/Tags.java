package youngsu5582.tool.ai_tracker.domain.tag;

import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;

public record Tags(List<Tag> tags) {

    public Optional<Tag> findTag(String name) {
        if(!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        return tags.stream()
            .filter(t -> t.getName().equals(name))
            .findFirst();
    }

    public List<String> tagNames() {
        return tags.stream().map(Tag::getName).toList();
    }

}
