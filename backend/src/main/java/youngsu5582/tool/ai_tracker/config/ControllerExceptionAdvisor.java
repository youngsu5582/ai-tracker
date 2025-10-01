package youngsu5582.tool.ai_tracker.config;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ControllerExceptionAdvisor {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e, HttpServletRequest request) {
        log.error("에러 발생: {}", e.getMessage(), e);

        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        detail.setTitle("Bad Request");
        detail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.badRequest().body(detail);
    }

}
