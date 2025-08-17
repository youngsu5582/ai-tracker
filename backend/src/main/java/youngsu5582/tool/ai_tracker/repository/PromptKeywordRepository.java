package youngsu5582.tool.ai_tracker.repository;

import youngsu5582.tool.ai_tracker.domain.PromptKeyword;

import java.util.List;
import java.util.Optional;

public interface PromptKeywordRepository {
    PromptKeyword save(PromptKeyword promptKeyword);
    Optional<PromptKeyword> findById(String id);
    List<PromptKeyword> findAll();
}