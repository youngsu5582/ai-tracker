package youngsu5582.tool.ai_tracker.domain.category;

import java.util.List;
import java.util.Optional;

public record Categories(
    List<Category> categories
) {

    public Optional<Category> findCategory(String name) {
        return categories.stream()
            .filter(c -> c.getName().equals(name))
            .findFirst();
    }

    public List<String> categoryNames() {
        return categories.stream().map(Category::getName).toList();
    }

}
