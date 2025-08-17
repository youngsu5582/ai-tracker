package youngsu5582.tool.ai_tracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import youngsu5582.tool.ai_tracker.config.FileStorageConfig;
import youngsu5582.tool.ai_tracker.domain.PromptKeyword;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.enabled", havingValue = "true")
public class FileBasedPromptKeywordRepository implements PromptKeywordRepository {

    private final FileStorageConfig fileStorageConfig;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true); // For pretty-printing JSON

    private Path keywordsDirectory;

    @PostConstruct
    public void init() {
        this.keywordsDirectory = Paths.get(fileStorageConfig.getDirectory(), "main_keywords");
        try {
            Files.createDirectories(keywordsDirectory);
            log.info("Initialized FileBasedPromptKeywordRepository with directory: {}", keywordsDirectory);
        } catch (IOException e) {
            log.error("Failed to create main_keywords directory: {}", keywordsDirectory, e);
            throw new RuntimeException("Failed to initialize main keyword repository", e);
        }
    }

    @Override
    public PromptKeyword save(PromptKeyword promptKeyword) {
        try {
            Path filePath = keywordsDirectory.resolve(promptKeyword.getId() + ".json");
            objectMapper.writeValue(filePath.toFile(), promptKeyword);
            return promptKeyword;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save prompt keyword", e);
        }
    }

    @Override
    public Optional<PromptKeyword> findById(String id) {
        try {
            Path path = keywordsDirectory.resolve(id + ".json");
            if (Files.exists(path)) {
                return Optional.of(objectMapper.readValue(path.toFile(), PromptKeyword.class));
            }
        } catch (IOException e) {
            log.error("Failed to read prompt keyword for {}: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<PromptKeyword> findAll() {
        try (Stream<Path> paths = Files.walk(keywordsDirectory)) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        return objectMapper.readValue(path.toFile(), PromptKeyword.class);
                    } catch (IOException e) {
                        log.error("Failed to read prompt keyword from file {}: {}", path, e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list all prompt keywords: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}