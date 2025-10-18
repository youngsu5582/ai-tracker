package youngsu5582.tool.ai_tracker.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import youngsu5582.tool.ai_tracker.MockEntityFactory;

class CategoriesTests {

    Categories categories;

    Category category1;
    Category category2;

    @BeforeEach
    void setUp() {
        category1 = MockEntityFactory.createCategory("카테고리1");
        category2 = MockEntityFactory.createCategory("카테고리2");
        categories = new Categories(
            List.of(category1, category2)
        );
    }

    @Test
    @DisplayName("카테고리를 이름으로 찾는다")
    void findTag() {
        var findCategory = categories.findCategory("카테고리1");
        assertThat(findCategory).isPresent().get().isEqualTo(category1);
    }


    @Test
    @DisplayName("이름에 해당하는 카테고리가 없으면 optional 을 반환한다")
    void findTagWithNotExistName() {
        var findCategory = categories.findCategory("없는 카테고리");
        assertThat(findCategory).isEmpty();
    }
}