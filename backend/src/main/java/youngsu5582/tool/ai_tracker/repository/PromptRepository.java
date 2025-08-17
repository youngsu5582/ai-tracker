package youngsu5582.tool.ai_tracker.repository;

import youngsu5582.tool.ai_tracker.domain.Prompt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PromptRepository {
    Prompt save(Prompt prompt);

    Optional<Prompt> findById(String id);

    List<Prompt> findAll();

    void deleteById(String id);

    List<Prompt> findByTimestampBetween(Instant start, Instant end);

    List<Prompt> findByCategory(String category);

    List<Prompt> findByTimestampAfter(Instant start);

    List<Prompt> findByConversationId(String conversationId);
}
