package youngsu5582.tool.ai_tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import youngsu5582.tool.ai_tracker.domain.Prompt;

import java.time.Instant;
import java.util.List;

@Repository
public interface PromptRepository extends MongoRepository<Prompt, String> {

    List<Prompt> findByTimestampBetween(Instant start, Instant end);

    List<Prompt> findByCategory(String category);

    List<Prompt> findByTimestampAfter(Instant start);
}
