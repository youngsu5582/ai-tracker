package youngsu5582.tool.ai_tracker.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import youngsu5582.tool.ai_tracker.application.event.PromptAnalysisCompletedEvent;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptRepository;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.document.PromptDocument;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.repository.PromptSearchRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptDocumentService {

    private final PromptSearchRepository promptSearchRepository;
    private final PromptRepository promptRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void analysis(PromptAnalysisCompletedEvent event) {
        long promptId = event.promptId();
        log.info("프롬프트 문서화를 시작 합니다. id: {}", promptId);
        var prompt = promptRepository.getByIdOrThrow(promptId);
        var promptDocument = promptSearchRepository.save(PromptDocument.from(prompt));
        log.info("프롬프트 문서를 저장 했습니다. id: {}, documentId: {}, result: {}", promptId, promptDocument.getId(), promptDocument);
    }
}
