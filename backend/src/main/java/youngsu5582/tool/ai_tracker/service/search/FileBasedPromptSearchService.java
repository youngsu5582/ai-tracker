package youngsu5582.tool.ai_tracker.service.search;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import youngsu5582.tool.ai_tracker.domain.TagIndex;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.repository.TagRepository;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "file.storage.enabled", havingValue = "true")
public class FileBasedPromptSearchService implements PromptSearchService {

    private final PromptRepository promptRepository;
    private final TagRepository tagRepository;

    @PostConstruct
    public void init() {
        log.info("Initializing FileBasedPromptSearchService: No longer building in-memory index. Relying on TagRepository.");
    }

    @Override
    public List<Prompt> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lowerCaseKeyword = keyword.toLowerCase();

        // Search by mainKeyword first
        List<Prompt> results = promptRepository.findAll().stream()
            .filter(p -> p.getMainKeyword() != null && p.getMainKeyword().equalsIgnoreCase(lowerCaseKeyword))
            .collect(Collectors.toList());

        // If no results from mainKeyword, search by tags
        if (results.isEmpty()) {
            return tagRepository.findById(lowerCaseKeyword)
                .map(tagIndex -> tagIndex.getPromptIds().stream()
                    .map(promptRepository::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        }
        return results;
    }

    @Override
    public void indexPrompt(Prompt prompt) {
        // Indexing is handled by TagIndexingListener and TagRepository
        log.debug("indexPrompt called on FileBasedPromptSearchService, but indexing is handled by TagIndexingListener.");
    }

    @Override
    public List<String> getAllMainKeywords() {
        return promptRepository.findAll().stream()
            .map(Prompt::getMainKeyword)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllTags() {
        return tagRepository.findAll().stream()
            .map(TagIndex::getKeyword)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }
}