package youngsu5582.tool.ai_tracker.spring_version_study;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
    properties = {
        "logging.structured.format.console=ecs",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
            + ",org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
    }
)
class LoggingVerificationTests {

    private final Logger logger = LoggerFactory.getLogger(LoggingVerificationTests.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("로그가 ECS JSON 형식으로 올바르게 출력된다.")
    void structuredLogging_ecsFormat_isCorrect(CapturedOutput output)
        throws JsonProcessingException {
        // given
        String testMessage = "This is a structured log test.";

        // when
        logger.info(testMessage);

        // then
        String logLine = output.getOut().lines()
            .filter(line -> line.contains(testMessage))
            .findFirst()
            .orElse(null);

        assertThat(logLine).isNotNull();

        JsonNode logJson = objectMapper.readTree(logLine);
        assertThat(logJson.has("@timestamp")).isTrue();
        assertThat(logJson.path("log").has("level")).isTrue();
        assertThat(logJson.path("log").get("level").asText()).isEqualTo("INFO");
        assertThat(logJson.has("message")).isTrue();
        assertThat(logJson.get("message").asText()).isEqualTo(testMessage);
        assertThat(logJson.path("process").path("thread").has("name")).isTrue();
        assertThat(logJson.path("ecs").has("version")).isTrue();
    }
}
