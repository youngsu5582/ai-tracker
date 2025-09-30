package youngsu5582.tool.ai_tracker.presentation.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import youngsu5582.tool.ai_tracker.application.service.IngestionService;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DataController {

    private final IngestionService ingestionService;

    @PostMapping(value = "/api/v1/prompts", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> prompts(@RequestBody CaptureRequest request) {
        log.info("HI DATA: {}", request);
        ingestionService.accept(request);
        return ResponseEntity.accepted()
            .build();
    }

}
