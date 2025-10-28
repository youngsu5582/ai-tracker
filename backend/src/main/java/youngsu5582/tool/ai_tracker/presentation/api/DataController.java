package youngsu5582.tool.ai_tracker.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import youngsu5582.tool.ai_tracker.application.service.IngestionService;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureResponse;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DataController {

    private final IngestionService ingestionService;

    @PostMapping(value = "/api/v1/prompts", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<CaptureResponse> prompts(@Valid @RequestBody CaptureRequest request) {
        log.debug("Request metadata: {}", request.getMetadata());
        log.info("Request received. ID: {}, conversationId: {}", request.getId(), request.getConversationId());
        UUID uuid = ingestionService.accept(request);
        return ResponseEntity.accepted().body(new CaptureResponse(uuid));
    }
}
