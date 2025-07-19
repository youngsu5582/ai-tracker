package youngsu5582.tool.ai_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import youngsu5582.tool.ai_tracker.api.dto.RemainingBalanceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class UsageService {

    private final WebClient webClient;
    private final String openaiApiKey;

    public UsageService(WebClient.Builder webClientBuilder,
        @Value("${openai.api.key}") String apiKey) {
        log.info("Initializing UsageService with API Key: {}", apiKey.substring(0, 16));
        this.openaiApiKey = apiKey;
        this.webClient = webClientBuilder
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader("Authorization", "Bearer " + openaiApiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    public Mono<RemainingBalanceResponse> getOpenAiRemainingBalance() {
        // This is a mock implementation as OpenAI does not provide a public API for remaining balance.
        // In a real-world scenario, you would call OpenAI's internal usage API if available,
        // or calculate usage based on your own stored prompt data.
        return Mono.just(new RemainingBalanceResponse(
            new BigDecimal("50.00"), // remainingAmount
            "USD", // currency
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // lastUpdated
        ));
    }
}