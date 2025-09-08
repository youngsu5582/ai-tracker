package youngsu5582.tool.ai_tracker.spring_version_study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import youngsu5582.tool.ai_tracker.presentation.HealthCheckController;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(HealthCheckController.class)
class HealthCheckControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckControllerTests.class);

    @Autowired
    private MockMvcTester mvc;

    @Test
    @DisplayName("MockMvcTester를 사용하여 /health GET 요청이 성공하고 'OK'를 반환한다.")
    void healthCheck_returnsOk() {
        // when
        var response = mvc.get().uri("/health").exchange();

        // then
        assertThat(response).hasStatus2xxSuccessful()
            .body().asString().isEqualTo("OK");

        // Log the thread name to observe virtual thread usage
        logger.info("Test execution thread: {}", Thread.currentThread());
    }
}
