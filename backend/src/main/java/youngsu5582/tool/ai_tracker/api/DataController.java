
package youngsu5582.tool.ai_tracker.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import youngsu5582.tool.ai_tracker.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.service.PromptService;
import youngsu5582.tool.ai_tracker.service.StatisticsService;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // WARNING: For development only. Restrict to specific origins in production.
public class DataController {

    private final PromptService promptService;
    private final StatisticsService statisticsService;

    @PostMapping("/capture")
    public ResponseEntity<Void> captureData(@RequestBody CaptureRequest request) {
        promptService.savePrompt(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(statisticsService.getStatistics(period));
    }
}

