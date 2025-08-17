package youngsu5582.tool.ai_tracker.repository;

import youngsu5582.tool.ai_tracker.domain.TagIndex;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    TagIndex save(TagIndex tagIndex);
    Optional<TagIndex> findById(String tag);
    List<TagIndex> findAll();
}