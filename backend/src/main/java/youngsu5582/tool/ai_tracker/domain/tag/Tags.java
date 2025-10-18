package youngsu5582.tool.ai_tracker.domain.tag;

import java.util.List;
import java.util.Optional;

public record Tags(List<Tag> tags) {

    public Optional<Tag> findTag(String name) {
        return tags.stream()
            .filter(t -> t.getName().equals(name))
            .findFirst();
    }

    public List<String> tagNames() {
        return tags.stream().map(Tag::getName).toList();
    }

}
