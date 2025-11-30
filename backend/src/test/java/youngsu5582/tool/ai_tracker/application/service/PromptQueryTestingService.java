package youngsu5582.tool.ai_tracker.application.service;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.util.StringUtils;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.document.PromptDocument;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@TestComponent
@RequiredArgsConstructor
public class PromptQueryTestingService {

    private final OpenSearchClient openSearchClient;

    private static final String INDEX_NAME = resolveIndexName();

    public List<PromptDocument> searchWithTag(String tag) {
        final Query matchQuery = Query.of(q -> q.match(
                m -> m.field("tags").query(FieldValue.of(tag))));
        return executeSearch(matchQuery);
    }

    public List<PromptDocument> searchWithPayload(String payload) {
        final Query matchQuery = Query.of(q -> q.match(m -> m.field("payload").query(FieldValue.of(payload))));
        return executeSearch(matchQuery);
    }


    public List<PromptDocument> searchAll() {
        final Query matchQuery = Query.of(q -> q.matchAll(MatchAllQuery.builder().build()));
        return executeSearch(matchQuery);
    }

    public List<PromptDocument> searchByCategory(String category) {
        final Query matchQuery = Query.of(q -> q.match(m -> m.field("category").query(FieldValue.of(category))));
        return executeSearch(matchQuery);
    }

    private List<PromptDocument> executeSearch(final Query query) {
        final SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(query);

        try {
            final SearchResponse<PromptDocument> response = openSearchClient.search(builder.build(), PromptDocument.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Search failed for index %s".formatted(INDEX_NAME), ioException);
        }
    }

    public void deleteAllDocuments() {
        try {
            openSearchClient.deleteByQuery(
                    DeleteByQueryRequest.of(builder -> builder.index(INDEX_NAME)
                            .query(q -> q.matchAll(m -> m))));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete documents for index %s".formatted(INDEX_NAME), e);
        }
    }

    private static String resolveIndexName() {
        Document annotation = PromptDocument.class.getAnnotation(Document.class);
        if (annotation == null || !StringUtils.hasText(annotation.indexName())) {
            throw new IllegalStateException("PromptDocument must declare an indexName");
        }
        return annotation.indexName();
    }
}
