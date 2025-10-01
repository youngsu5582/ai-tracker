package youngsu5582.tool.ai_tracker.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import youngsu5582.tool.ai_tracker.application.service.IngestionService;

@AutoConfigureMockMvc
@WebMvcTest
public abstract class ControllerTestSupport {

    protected final ObjectMapper objectMapper = new ObjectMapper()
        .registerModules(new JavaTimeModule());

    @Autowired
    protected MockMvcTester mvc;

    @MockitoBean
    protected IngestionService ingestionService;
}
