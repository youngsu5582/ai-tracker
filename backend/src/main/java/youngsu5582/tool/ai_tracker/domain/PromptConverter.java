package youngsu5582.tool.ai_tracker.domain;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import youngsu5582.tool.ai_tracker.api.dto.CaptureRequest;

@Component
public class PromptConverter {

    public Prompt convert(CaptureRequest request) {
        LocalDateTime timestamp = LocalDateTime.parse(request.timestamp(),
            DateTimeFormatter.ISO_DATE_TIME);

        return new Prompt(null, request.conversationId(), request.prompt(), request.response(),
            request.model(),
            request.source(), timestamp.toInstant(ZoneOffset.UTC), null, null, null,
            null, request.language(), null);
    }

}
