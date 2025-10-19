package youngsu5582.tool.ai_tracker.domain.category;

import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;

public record Categories(
    List<Category> categories
) {

    public Optional<Category> findCategory(String name) {
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        return categories.stream()
            .filter(c -> name.equals(c.getName()))
            .findFirst();
    }

    public List<String> categoryNames() {
        return categories.stream().map(Category::getName).toList();
    }

}
