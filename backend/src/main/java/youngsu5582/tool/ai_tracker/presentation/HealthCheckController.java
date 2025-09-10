package youngsu5582.tool.ai_tracker.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthCheckController {

    @GetMapping(value = "/health",produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> healthCheck() {
        log.info("Health check endpoint called by thread: {}", Thread.currentThread());
        return ResponseEntity.ok("OK");
    }
}
