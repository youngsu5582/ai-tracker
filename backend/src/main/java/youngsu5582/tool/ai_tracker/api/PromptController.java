package youngsu5582.tool.ai_tracker.api;

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import youngsu5582.tool.ai_tracker.domain.Prompt;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;
import youngsu5582.tool.ai_tracker.service.PromptService;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // WARNING: For development only. Restrict to specific origins in production.
public class PromptController {

    private final PromptRepository promptRepository;
    private final PromptService promptService;

    @GetMapping
    public ResponseEntity<List<Prompt>> getPromptsByCategory(@RequestParam String category) {
        return ResponseEntity.ok(promptRepository.findByCategory(category));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Prompt>> getHistory(
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}") String startDate,
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}") String endDate
    ) {
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);

        // Define the user's local time zone (assuming KST for the user's context)
        ZoneId userTimeZone = ZoneId.of("Asia/Seoul");

        // Convert the start of the local day to UTC Instant
        Instant startDateTimeUtc = startLocalDate.atStartOfDay(userTimeZone).toInstant();

        // Convert the end of the local day to UTC Instant
        Instant endDateTimeUtc = endLocalDate.atTime(23, 59, 59, 999_999_999).atZone(userTimeZone).toInstant();

        return ResponseEntity.ok(promptRepository.findByTimestampBetween(startDateTimeUtc, endDateTimeUtc));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Prompt>> getPromptsByConversationId(@PathVariable String conversationId) {
        return ResponseEntity.ok(promptRepository.findByConversationId(conversationId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Prompt>> searchPrompts(@RequestParam String keyword) {
        return ResponseEntity.ok(promptService.searchPrompts(keyword));
    }

    @GetMapping("/keywords")
    public ResponseEntity<List<String>> getAllKeywords() {
        return ResponseEntity.ok(promptService.getAllMainKeywords());
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        return ResponseEntity.ok(promptService.getAllTags());
    }
}
