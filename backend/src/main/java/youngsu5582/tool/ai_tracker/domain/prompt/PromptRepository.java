package youngsu5582.tool.ai_tracker.domain.prompt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    Optional<Prompt> findByUuid(UUID uuid);

    List<Prompt> findByStatus(PromptStatus promptStatus);
}
