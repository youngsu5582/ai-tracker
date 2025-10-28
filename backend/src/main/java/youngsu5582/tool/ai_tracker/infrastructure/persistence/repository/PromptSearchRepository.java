package youngsu5582.tool.ai_tracker.infrastructure.persistence.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.document.PromptDocument;

public interface PromptSearchRepository extends ElasticsearchRepository<PromptDocument, String> {

}
