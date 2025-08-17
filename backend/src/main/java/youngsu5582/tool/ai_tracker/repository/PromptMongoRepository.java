package youngsu5582.tool.ai_tracker.repository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import youngsu5582.tool.ai_tracker.domain.Prompt;

@Repository
@ConditionalOnProperty(name = "file.storage.enabled", havingValue = "false", matchIfMissing = true)
public interface PromptMongoRepository extends MongoRepository<Prompt, String>, PromptRepository {

    Logger log = LoggerFactory.getLogger(PromptMongoRepository.class);

    @PostConstruct
    default void init() {
        log.info("PromptMongoRepository MongoDbRepository initialized");
    }
}
