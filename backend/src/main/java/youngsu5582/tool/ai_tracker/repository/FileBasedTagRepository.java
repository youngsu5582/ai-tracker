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
import youngsu5582.tool.ai_tracker.domain.TagIndex;

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
public class FileBasedTagRepository implements TagRepository {

    private final FileStorageConfig fileStorageConfig;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true); // For pretty-printing JSON

    private Path keywordsDirectory;

    @PostConstruct
    public void init() {
        this.keywordsDirectory = Paths.get(fileStorageConfig.getDirectory(), "keywords");
        try {
            Files.createDirectories(keywordsDirectory);
            log.info("Initialized FileBasedKeywordRepository with directory: {}", keywordsDirectory);
        } catch (IOException e) {
            log.error("Failed to create keywords directory: {}", keywordsDirectory, e);
            throw new RuntimeException("Failed to initialize keyword repository", e);
        }
    }

    @Override
    public TagIndex save(TagIndex tagIndex) {
        try {
            Path filePath = keywordsDirectory.resolve(tagIndex.getKeyword() + ".json");
            objectMapper.writeValue(filePath.toFile(), tagIndex);
            return tagIndex;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save tag index", e);
        }
    }

    @Override
    public Optional<TagIndex> findById(String keyword) {
        try {
            Path path = keywordsDirectory.resolve(keyword + ".json");
            if (Files.exists(path)) {
                return Optional.of(objectMapper.readValue(path.toFile(), TagIndex.class));
            }
        } catch (IOException e) {
            log.error("Failed to read tag index for {}: {}", keyword, e.getMessage());
            // Consider not throwing here, just return empty optional if file is corrupted/unreadable
        }
        return Optional.empty();
    }

    @Override
    public List<TagIndex> findAll() {
        try (Stream<Path> paths = Files.walk(keywordsDirectory)) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        return objectMapper.readValue(path.toFile(), TagIndex.class);
                    } catch (IOException e) {
                        log.error("Failed to read tag index from file {}: {}", path, e.getMessage());
                        return null; // Return null for unreadable files
                    }
                })
                .filter(java.util.Objects::nonNull) // Filter out nulls from unreadable files
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list all tag indexes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}