package youngsu5582.tool.ai_tracker.spring_version_study;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class MockitoBeanTests {

    @Test
    @DisplayName("@MockBean 어노테이션은 3.4 부터 Deprecated + for removal 처리가 되었다.")
    void mockBeanAnnotationIsDeprecated() {
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
