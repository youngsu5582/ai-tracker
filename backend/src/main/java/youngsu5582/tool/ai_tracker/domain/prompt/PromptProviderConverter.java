package youngsu5582.tool.ai_tracker.domain.prompt;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class PromptProviderConverter implements AttributeConverter<PromptProvider, String> {

    @Override
    public String convertToDatabaseColumn(PromptProvider attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getAlias();
    }

    @Override
    public PromptProvider convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(PromptProvider.values())
            .filter(c -> c.getAlias().equals(dbData))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown PromptProvider alias: " + dbData));
    }
}
