package youngsu5582.tool.ai_tracker.service;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import youngsu5582.tool.ai_tracker.domain.Prompt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import youngsu5582.tool.ai_tracker.repository.PromptRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PromptRepository promptRepository;

    public Map<String, Object> getStatistics(String period) {
        Instant now = Instant.now();
        Instant startInstant = switch (period.toLowerCase()) {
            case "weekly" -> LocalDateTime.now().with(WeekFields.ISO.dayOfWeek(), 1).toLocalDate()
                .atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
            case "monthly" -> LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay()
                .atZone(ZoneOffset.UTC).toInstant();
            default ->
                LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
        };

        List<Prompt> prompts = promptRepository.findByTimestampBetween(startInstant, now);

        Map<String, Object> statisticsMap = Map.of(
            "totalRequests", prompts.size(),
            "requestsBySite", getRequestsBySite(prompts),
            "requestsByModel", getRequestsByModel(prompts),
            "requestsByCategory", getRequestsByCategory(prompts),
            "topSites", getTopN(prompts, Prompt::getSource, 5),
            "topModels", getTopN(prompts, Prompt::getModel, 5),
            "requestsByHour", getRequestsByHour(prompts),
            "categoryTree", getCategoryTree(prompts)
        );
        log.info("Statistics generated for period {}: {}", period, statisticsMap);
        return statisticsMap;
    }

    private Map<String, Long> getRequestsBySite(List<Prompt> prompts) {
        return prompts.stream()
            .collect(
                Collectors.groupingBy(
                    p -> Optional.ofNullable(p.getSource()).orElse("Unknown Site"),
                    Collectors.counting()));
    }

    private Map<String, Long> getRequestsByModel(List<Prompt> prompts) {
        return prompts.stream()
            .collect(
                Collectors.groupingBy(
                    p -> Optional.ofNullable(p.getModel()).orElse("Unknown Model"),
                    Collectors.counting()));
    }

    private Map<String, Long> getRequestsByCategory(List<Prompt> prompts) {
        return prompts.stream()
            .collect(Collectors.groupingBy(
                p -> Optional.ofNullable(p.getCategory()).orElse("Uncategorized"),
                Collectors.counting()));
    }

    private Map<String, Long> getTopN(List<Prompt> prompts, Function<Prompt, String> classifier,
        int n) {
        return prompts.stream()
            .collect(Collectors.groupingBy(classifier, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(n)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                java.util.LinkedHashMap::new));
    }

    private Map<Integer, Long> getRequestsByHour(List<Prompt> prompts) {
        Map<Integer, Long> requestsByHour = prompts.stream()
            .collect(Collectors.groupingBy(p -> p.getTimestamp().atZone(ZoneOffset.UTC).getHour(),
                Collectors.counting()));

        // Ensure all hours from 0 to 23 are present
        IntStream.range(0, 24).forEach(hour -> requestsByHour.putIfAbsent(hour, 0L));

        return requestsByHour;
    }

    private Map<String, Long> getCategoryTree(List<Prompt> prompts) {
        // This is a simplified version. For a real tree, you'd need a more complex data structure.
        return getRequestsByCategory(prompts);
    }
}
