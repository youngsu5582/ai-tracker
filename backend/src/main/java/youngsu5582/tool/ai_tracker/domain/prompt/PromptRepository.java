package youngsu5582.tool.ai_tracker.domain.prompt;

import org.springframework.data.jpa.repository.JpaRepository;
import youngsu5582.tool.ai_tracker.common.exception.EntityNotExistException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    Optional<Prompt> findByUuid(UUID uuid);

    List<Prompt> findByStatus(PromptStatus promptStatus);

    Optional<Prompt> findByMessageId(String messageId);

    default Prompt getByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new EntityNotExistException(Prompt.class, id));
    }
}
