package youngsu5582.tool.ai_tracker.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import youngsu5582.tool.ai_tracker.config.FileStorageConfig;
import youngsu5582.tool.ai_tracker.domain.Prompt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.enabled", havingValue = "true")
public class FileBasedPromptRepository implements PromptRepository {

    private final FileStorageConfig fileStorageConfig;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @PostConstruct
    public void init() {
        log.info("Initializing FileBasedPromptRepository with directory: {}",
            fileStorageConfig.getDirectory());
    }

    @Override
    public Prompt save(Prompt prompt) {
        if (prompt.getId() == null) {
            prompt.setId(UUID.randomUUID().toString());
        }
        try {
            Path directoryPath = Paths.get(fileStorageConfig.getDirectory());
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            Path filePath = directoryPath.resolve(prompt.getId() + ".json");
            objectMapper.writeValue(filePath.toFile(), prompt);
            return prompt;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save prompt", e);
        }
    }

    @Override
    public Optional<Prompt> findById(String id) {
        try {
            Path path = Paths.get(fileStorageConfig.getDirectory(), id + ".json");
            if (Files.exists(path)) {
                return Optional.of(objectMapper.readValue(path.toFile(), Prompt.class));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read prompt", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prompt> findAll() {
        try (Stream<Path> paths = Files.walk(Paths.get(fileStorageConfig.getDirectory()))) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        return objectMapper.readValue(path.toFile(), Prompt.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read prompt", e);
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Prompt> findByTimestampBetween(java.time.Instant start, java.time.Instant end) {
        return findAll().stream()
            .filter(p -> p.getTimestamp() != null && p.getTimestamp().isAfter(start) && p.getTimestamp().isBefore(end))
            .collect(Collectors.toList());
    }

    @Override
    public List<Prompt> findByCategory(String category) {
        return findAll().stream()
            .filter(p -> category.equals(p.getCategory()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Prompt> findByTimestampAfter(java.time.Instant start) {
        return findAll().stream()
            .filter(p -> p.getTimestamp().isAfter(start))
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        try {
            Path path = Paths.get(fileStorageConfig.getDirectory(), id + ".json");
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete prompt", e);
        }
    }

    @Override
    public List<Prompt> findByConversationId(String conversationId) {
        return findAll().stream()
            .filter(p -> conversationId.equals(p.getConversationId()))
            .collect(Collectors.toList());
    }
}