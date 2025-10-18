package youngsu5582.tool.ai_tracker.domain.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import youngsu5582.tool.ai_tracker.MockEntityFactory;

class TagsTests {

    private Tags tags;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = MockEntityFactory.createTag("태그1");
        tag2 = MockEntityFactory.createTag("태그2");
        tags = new Tags(
            List.of(tag1, tag2)
        );
    }

    @Test
    @DisplayName("태그를 이름으로 찾는다")
    void findTag() {
        var findTag = tags.findTag("태그1");
        assertThat(findTag).isPresent().get().isEqualTo(tag1);
    }


    @Test
    @DisplayName("이름에 해당하는 태그가 없으면 optional 을 반환한다")
    void findTagWithNotExistName() {
        var findTag = tags.findTag("없는 태그");
        assertThat(findTag).isEmpty();
    }
}
