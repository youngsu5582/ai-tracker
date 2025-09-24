package youngsu5582.tool.ai_tracker.common.entity;

import java.time.Instant;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

public abstract class AuditEntity {

    @CreatedBy
    Instant createdAt;

    @LastModifiedBy
    Instant updatedAt;

    boolean deleted;
}
