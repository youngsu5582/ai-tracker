package youngsu5582.tool.ai_tracker.domain;

import java.time.Instant;
import org.springframework.stereotype.Component;
import youngsu5582.tool.ai_tracker.api.dto.CaptureRequest;

@Component
public class PromptConverter {

    public Prompt convert(CaptureRequest request) {

        Instant instant = Instant.parse(request.timestamp());

        return Prompt.builder()
            .conversationId(request.conversationId())
            .prompt(request.prompt())
            .response(request.response())
            .model(request.model())
            .source(request.source())
            .timestamp(instant)
            .language(request.language())
            .build();
    }

}
