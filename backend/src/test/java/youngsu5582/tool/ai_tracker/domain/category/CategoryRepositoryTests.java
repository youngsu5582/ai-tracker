package youngsu5582.tool.ai_tracker.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import youngsu5582.tool.ai_tracker.MockEntityFactory;
import youngsu5582.tool.ai_tracker.support.IntegrationTestSupport;

class CategoryRepositoryTests extends IntegrationTestSupport {

    @Autowired
    CategoryRepository categoryRepository;

    Category child;

    @BeforeEach
    void setUp() {
        var root = categoryRepository.save(MockEntityFactory.createCategory("Root"));
        var child1 = categoryRepository.save(MockEntityFactory.createCategory("Child", root));
        var child2 = categoryRepository.save(MockEntityFactory.createCategory("Child2", child1));
        child = categoryRepository.save(MockEntityFactory.createCategory("Child3", child2));
    }

    @Test
    @Transactional
    @DisplayName("카테고리는 부모를 가지는 계층적 구조로 관리된다.")
    void categoryHierarchy() {
        var findResultOpt = categoryRepository.findByUuid(child.getUuid());
        assertThat(findResultOpt).isPresent();
        var findResult = findResultOpt.get();

        var categories = new ArrayList<Category>();
        var next = findResult;
        while (next != null) {
            categories.add(next);
            next = next.getParentCategory();
        }
        assertThat(categories).hasSize(4);
    }

}