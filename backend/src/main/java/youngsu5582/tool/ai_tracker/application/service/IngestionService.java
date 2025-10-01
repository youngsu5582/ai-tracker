package youngsu5582.tool.ai_tracker.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import youngsu5582.tool.ai_tracker.application.event.PromptReceivedEvent;
import youngsu5582.tool.ai_tracker.domain.prompt.Prompt;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptRepository;
import youngsu5582.tool.ai_tracker.domain.prompt.PromptStatus;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PromptRepository promptRepository;

    @Transactional
    public UUID accept(CaptureRequest captureRequest) {
        log.info("Accepting capture request with messageId: {}", captureRequest.getId());
        Prompt prompt = findOrSavePrompt(captureRequest);
        applicationEventPublisher.publishEvent(new PromptReceivedEvent(prompt.getId()));
        return prompt.getUuid();
    }

    private Prompt findOrSavePrompt(CaptureRequest captureRequest) {
        Prompt prompt = promptRepository.findByMessageId(captureRequest.getId())
            .orElseGet(() -> promptRepository.save(Prompt.builder()
                .status(PromptStatus.RECEIVED)
                .messageId(captureRequest.getId())
                .build()));
        prompt.updatePayload(captureRequest.getPayload());
        return prompt;
    }
}
