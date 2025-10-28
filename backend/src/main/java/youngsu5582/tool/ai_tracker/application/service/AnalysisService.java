package youngsu5582.tool.ai_tracker.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import youngsu5582.tool.ai_tracker.application.event.PromptReceivedEvent;
import youngsu5582.tool.ai_tracker.domain.category.Categories;
import youngsu5582.tool.ai_tracker.domain.category.Category;
import youngsu5582.tool.ai_tracker.domain.category.CategoryRepository;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptRepository;
import youngsu5582.tool.ai_tracker.domain.tag.Tag;
import youngsu5582.tool.ai_tracker.domain.tag.TagRepository;
import youngsu5582.tool.ai_tracker.domain.tag.Tags;
import youngsu5582.tool.ai_tracker.provider.PromptAnalysisProvider;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisMetadata.AnalysisMetadataAttribute;
import youngsu5582.tool.ai_tracker.provider.dto.AnalysisResult;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final PromptRepository promptRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final PromptAnalysisProvider promptAnalysisProvider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void analysis(PromptReceivedEvent event) {
        Prompt prompt = promptRepository.getByIdOrThrow(event.promptId());
        Categories categories = categoryRepository.findAllToFirstClass();
        Tags tags = tagRepository.findAllToFirstClass();

        try {
            // DB 에서 필요한 요소들 조회
            log.info("프롬프트 분석을 시작합니다. ID: {}", event.promptId());

            AnalysisMetadata metadata = new AnalysisMetadata(
                Map.of(AnalysisMetadataAttribute.CATEGORY_LIST, categories.categoryNames(),
                    AnalysisMetadataAttribute.TAG_LIST, tags.tagNames()
                )
            );

            // 프롬프트 분석한 결과
            AnalysisResult result = promptAnalysisProvider.analyze(prompt.getPayload(), metadata);
            log.info("분석한 응답: {}", result);

            // 데이터 설정
            Category promptCategory = findOrSaveCategory(categories, result.category(),
                result.parentCategory());
            List<Tag> promptTags = result.tagList()
                .stream().map(tag -> findOrSaveTag(tags, tag))
                .toList();
            prompt.completeAnalyze(promptCategory, promptTags);

            log.info("프롬프트 분석을 완료했습니다. ID: {}, 카테고리: {}, 태그 목록: {}", event.promptId(),
                promptCategory, promptTags);
        } catch (Exception e) {
            log.warn("프롬프트 분석을 실패했습니다! ID: {} 메시지: {}", event.promptId(), e.getMessage(), e);
            prompt.failAnalyze(e.getMessage());
        }
    }

    private Tag findOrSaveTag(Tags tags, String tagName) {
        return tags.findTag(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
    }

    // 카테고리는 태그와 다소 다르다...
    // 왜냐하면, 부모 카테고리라는 개념이 존재 - AI 한테 묻고, 정보를 받아와야 함
    // 부모 개념이 존재하는거 같다면? -> AI 한테 받아오게
    private Category findOrSaveCategory(Categories categories,
        String categoryName,
        String parentCategoryName) {
        Category parentCategory = categories.findCategory(parentCategoryName).orElse(null);
        return categories.findCategory(categoryName)
            .orElseGet(
                () -> categoryRepository.save(Category.builder()
                    .name(categoryName)
                    .parentCategory(parentCategory)
                    .build()));
    }
}
