package youngsu5582.tool.ai_tracker.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * 테스트 코드에서는 현재 스레드에서 발행한 이벤트만 {@code @RecordApplicationEvents} 어노테이션으로 캡처가 됨<br> 테스트가 수행된 스레드가 아닌
 * 다른 스레드에서 발행된 이벤트를 캡처하기 위해 만든 테스트 유틸 클래스
 */
@TestComponent
public class EventCaptureListener extends AbstractTestExecutionListener {

    private final List<Object> eventList = new CopyOnWriteArrayList<>();

    @EventListener
    public void captureEvent(Object event) {
        eventList.add(event);
    }

    public <T> List<T> findEventsOfType(Class<T> type) {
        return eventList.stream()
            .filter(type::isInstance)
            .map(type::cast)
            .toList();
    }

    public <T> T findEventOfType(Class<T> type) {
        return findEventsOfType(type).stream().findFirst().orElse(null);
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        eventList.clear();
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        eventList.clear();
    }
}
