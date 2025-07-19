package youngsu5582.tool.ai_tracker.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import youngsu5582.tool.ai_tracker.api.dto.RemainingBalanceResponse;
import youngsu5582.tool.ai_tracker.service.UsageService;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/openai/balance")
    public Mono<ResponseEntity<RemainingBalanceResponse>> getOpenAiRemainingBalance() {
        return usageService.getOpenAiRemainingBalance()
            .map(ResponseEntity::ok);
    }
}