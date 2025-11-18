package youngsu5582.tool.ai_tracker.presentation.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult;
import youngsu5582.tool.ai_tracker.support.ControllerTestSupport;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromptQueryControllerTests extends ControllerTestSupport {

    @Test
    @DisplayName("검색 API는 질의 조건을 서비스로 전달하고 검색 결과를 반환한다.")
    void search_prompts_withQueryParameters() {
        // given

        PromptSearchResult result = getPromptSummaryList();
        when(promptQueryService.search(any())).thenReturn(result);

        // when
        var response = mvc.perform(
                MockMvcRequestBuilders.get("/api/v1/prompts/search?searchText=latency")
                        .accept(MediaType.APPLICATION_JSON));

        // then
        assertThat(response).hasStatus2xxSuccessful();
    }

    @Test
    @DisplayName("검색 API는 질의 조건을 서비스로 전달하고 검색 결과를 반환한다.")
    void search_prompts_withQueryParameters_from_to() {
        // given

        PromptSearchResult result = getPromptSummaryList();
        when(promptQueryService.search(any())).thenReturn(result);

        // when
        var response = mvc.perform(
                MockMvcRequestBuilders.get("/api/v1/prompts/search?searchText=latency&from=2024-01-01T00:00:00Z&to=2024-12-31T23:59:59Z")
                        .accept(MediaType.APPLICATION_JSON));

        // then
        verify(promptQueryService).search(argThat(cmd ->
                "latency".equals(cmd.searchText()) &&
                        Instant.parse("2024-01-01T00:00:00Z").equals(cmd.from()) &&
                        Instant.parse("2024-12-31T23:59:59Z").equals(cmd.to())
        ));
        assertThat(response).hasStatus2xxSuccessful();
    }

    @Test
    @DisplayName("모든 결과를 반환한다.")
    void get_all_prompts() {
        // given

        PromptSearchResult result = getPromptSummaryList();
        when(promptQueryService.getAll()).thenReturn(result);

        // when
        var response = mvc.perform(
                MockMvcRequestBuilders.get("/api/v1/prompts/all")
                        .accept(MediaType.APPLICATION_JSON));

        // then
        assertThat(response).hasStatus2xxSuccessful();
    }

    private PromptSearchResult getPromptSummaryList() {
        return new PromptSearchResult(
                1L,
                List.of(new PromptSearchResult.PromptSummary(
                        "uuid-1",
                        42L,
                        "message-123",
                        "COMPLETED",
                        "OPENAI",
                        "Generative AI",
                        "Architecture",
                        List.of("java", "prompt-engineering"),
                        Instant.parse("2024-05-01T10:00:00Z"),
                        Instant.parse("2024-05-01T11:00:00Z"),
                        "Investigate latency patterns with java prompt-engineering techniques.",
                        null
                ))
        );
    }
}
