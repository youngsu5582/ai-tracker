package youngsu5582.tool.ai_tracker.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import youngsu5582.tool.ai_tracker.application.service.PromptQueryService;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult;
import youngsu5582.tool.ai_tracker.presentation.api.dto.PromptSearchRequest;
import youngsu5582.tool.ai_tracker.presentation.api.dto.PromptSearchResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/prompts", produces = APPLICATION_JSON_VALUE)
public class PromptQueryController {

    private final PromptQueryService promptQueryService;

    @GetMapping("/search")
    public PromptSearchResponse search(@Valid PromptSearchRequest request) {
        log.info("search request: {}", request);
        PromptSearchResult result = promptQueryService.search(request.toCommand());
        log.info("search result: {}", result.getPromptIds());
        return PromptSearchResponse.from(result);
    }

    @GetMapping("/all")
    public PromptSearchResponse searchAll() {
        log.info("searchAll request");
        PromptSearchResult result = promptQueryService.getAll();
        log.info("searchAll result: {}", result.getPromptIds());
        return PromptSearchResponse.from(result);
    }
}
