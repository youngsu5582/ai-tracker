package youngsu5582.tool.ai_tracker.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@ConditionalOnProperty(name = "file.storage.enabled", havingValue = "true")
@Data
public class FileStorageConfig {
    private String directory;
}
