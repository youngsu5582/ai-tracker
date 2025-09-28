package youngsu5582.tool.ai_tracker.domain.tag;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByUuid(UUID uuid);
}
