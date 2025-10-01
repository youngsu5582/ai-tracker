package youngsu5582.tool.ai_tracker.presentation.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import youngsu5582.tool.ai_tracker.MockEntityFactory;
import youngsu5582.tool.ai_tracker.presentation.api.dto.CaptureRequest;
import youngsu5582.tool.ai_tracker.support.ControllerTestSupport;

class DataControllerTests extends ControllerTestSupport {

    @Test
    @DisplayName("프롬프트 수집 API를 호출하면 HTTP 상태 코드 202(Accepted)를 반환한다.")
    void promptsApiTest() throws Exception {
        // given
        // 테스트에 사용할 요청 객체를 생성합니다.
        CaptureRequest request = MockEntityFactory.getCaptureRequest();

        // when & then
        // MockMvc를 사용하여 API를 호출하고 결과를 검증합니다.
        mvc.perform(
                post("/api/v1/prompts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)) // 객체를 JSON 문자열로 변환하여 본문에 추가
            )
            // 응답 상태가 202 Accepted 인지 확인
            .assertThat().hasStatus(202);

        // IngestionService의 accept 메소드가 CaptureRequest 타입의 어떤 객체로든 한 번 호출되었는지 검증합니다.
        then(ingestionService).should().accept(eq(request));
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 요청 시 HTTP 400을 반환한다.")
    void invalidJsonTest() throws Exception {
        mvc.perform(
                post("/api/v1/prompts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("key", "value"))))
            .assertThat().hasStatus4xxClientError();
    }
}
