package youngsu5582.tool.ai_tracker.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptQueryLogAppender {

    public void appendErrorLog(OpenSearchException e) {
        var err = e.error();
        var root = err != null && !err.rootCause().isEmpty()
                ? err.rootCause().getFirst() : null;
        if (root != null) {
            log.error("root_cause type={}, reason={}", root.type(), root.reason());
        }
        log.error("OS error status={}, type={}, reason={}", e.status(), (err != null ? err.type() : "n/a"), (err != null ? err.reason() : "n/a"), e);
    }
}
