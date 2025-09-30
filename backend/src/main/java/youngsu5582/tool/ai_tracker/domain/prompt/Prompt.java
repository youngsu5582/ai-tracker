package youngsu5582.tool.ai_tracker.domain.prompt;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import youngsu5582.tool.ai_tracker.common.entity.AuditEntity;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;

@Entity
@Table(name = "prompts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE prompts SET deleted = true WHERE id = ?")
@EntityListeners(AuditingEntityListener.class)
public class Prompt extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, insertable = false)
    private Long id;

    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromptStatus status = PromptStatus.RECEIVED;

    private String payload;

    // 제공자가 제공해주는 메시지 ID
    private String messageId;

    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PromptTag> promptTags = new HashSet<>();

    public void addTag(Tag tag) {
        PromptTag promptTag = new PromptTag(this, tag);
        this.promptTags.add(promptTag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(uuid, ((Prompt) o).uuid);
    }

    // https://thorben-janssen.com/lombok-hibernate-how-to-avoid-common-pitfalls/?utm_source=chatgpt.com 참고

    // Primary Key 불일치
    // Lombok 의 equalsAndHashCode 는 클래스 모든 필드를 사용해 equals, hashCode 메소드 생성
    // hashCode 는 null 이였던 id 값 기반으로 계산
    // DB save 하면서 id 가 채워지면 hashCode 가 깨져서 hashMap, hashSet 에 넣으면 못 찾게 될 수 있다.
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
