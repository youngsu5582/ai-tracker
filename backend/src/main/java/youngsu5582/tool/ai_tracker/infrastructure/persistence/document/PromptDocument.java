package youngsu5582.tool.ai_tracker.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import youngsu5582.tool.ai_tracker.domain.category.Category;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptTag;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "prompts")
public class PromptDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long promptId;

    @Field(type = FieldType.Keyword)
    private String messageId;

    @Field(type = FieldType.Keyword)
    private String provider;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String payload;

    @Field(type = FieldType.Keyword)
    private String error;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String parentCategory;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant analyzedAt;

    public static PromptDocument from(Prompt prompt) {
        Objects.requireNonNull(prompt, "prompt must not be null");

        Category category = prompt.getCategory();
        Category parentCategory = category != null ? category.getParentCategory() : null;

        List<String> tagNames = prompt.getPromptTags()
                .stream()
                .map(PromptTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return PromptDocument.builder()
                .id(prompt.getUuid().toString())
                .promptId(prompt.getId())
                .messageId(prompt.getMessageId())
                .provider(prompt.getProvider().getAlias())
                .status(prompt.getStatus().name())
                .payload(prompt.getPayload())
                .error(prompt.getError())
                .category(category != null ? category.getName() : null)
                .parentCategory(parentCategory != null ? parentCategory.getName() : null)
                .tags(tagNames)
                .createdAt(prompt.getCreateTime())
                .analyzedAt(Instant.now())
                .build();
    }
}
