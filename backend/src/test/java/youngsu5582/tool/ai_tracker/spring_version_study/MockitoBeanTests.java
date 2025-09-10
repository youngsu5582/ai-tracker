package youngsu5582.tool.ai_tracker.spring_version_study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.SpringVersion;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class MockitoBeanTests {

    @Test
    @DisplayName("@MockBean 어노테이션은 3.4 부터 Deprecated + for removal 처리가 되었다.")
    void mockBeanAnnotationIsDeprecated() {
        String version = SpringVersion.getVersion();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);

        // 6.2 이상은 3.4 로 취급한다.

        // Assumptions 를 통해 스프링 버전이 다르면, 아예 검증하지 않는다.
        // 테스트 코드가 아닌, 테스트 환경에 대한 검증
        assumeTrue(major > 6 || (major == 6 && minor >= 2),
            "Test is for Spring Framework 6.2+ (Spring Boot 3.4+)");
        // given
        Class<?> mockBeanClass = MockBean.class;

        // when
        boolean isDeprecated = mockBeanClass.isAnnotationPresent(Deprecated.class);

        // then
        assertThat(isDeprecated).isTrue();
    }

    @Test
    @DisplayName("@Mockito 어노테이션이 대신 사용된다.")
    void mockitoBeanAnnotation() {
        // given
        Class<?> mockBeanClass = MockitoBean.class;

        // when
        boolean isDeprecated = mockBeanClass.isAnnotationPresent(Deprecated.class);

        // then
        assertThat(isDeprecated).isFalse();
    }

}
