package youngsu5582.tool.ai_tracker.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import youngsu5582.tool.ai_tracker.MockEntityFactory;
import youngsu5582.tool.ai_tracker.application.event.PromptReceivedEvent;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class IngestionServiceTests extends IntegrationTestSupport {

    @Test
    @DisplayName("캡처 DTO 를 통해 프롬프트를 저장하고, 이벤트를 발행한다.")
    void savePrompt_PublishEvent() {

        CaptureRequest captureRequest = MockEntityFactory.getCaptureRequest();
        var messageId = captureRequest.getId();
        ingestionService.accept(captureRequest);

        var promptOpt = promptRepository.findByMessageId(messageId);
        assertThat(promptOpt).isPresent();
        var prompt = promptOpt.get();

        assertThat(prompt.getPayload()).isEqualTo(captureRequest.getPayload());
        var event = eventCaptureListener.findEventOfType(PromptReceivedEvent.class);
        assertThat(event.promptId()).isEqualTo(prompt.getId());
    }

}