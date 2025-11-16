package youngsu5582.tool.ai_tracker.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.stream.JsonGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.PlainJsonSerializable;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.TotalHits;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchCommand;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult;
import youngsu5582.tool.ai_tracker.application.service.dto.PromptSearchResult.PromptSummary;
import youngsu5582.tool.ai_tracker.infrastructure.persistence.document.PromptDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromptQueryService {

    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String PAYLOAD_FIELD = "payload";
    private static final String TAGS_FIELD = "tags";
    private static final String CATEGORY_FIELD = "category";
    private static final String PARENT_CATEGORY_FIELD = "parentCategory";
    private static final String INDEX_NAME = resolveIndexName();

    private final OpenSearchClient openSearchClient;
    private final PromptQueryLogAppender logAppender;
    private final ObjectMapper objectMapper;

    public PromptSearchResult getAll() {
        var matchAll = MatchAllQuery.builder().build();
        var query = Query.of(q -> q.matchAll(matchAll));
        SearchRequest request = SearchRequest.of(sr -> sr
                .query(query)
                .index(INDEX_NAME)
        );
        try {
            SearchResponse<PromptDocument> response = openSearchClient.search(request, PromptDocument.class);

            List<PromptSummary> prompts = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(this::toSummary)
                    .toList();

            long total = extractTotalHits(response.hits().total(), prompts.size());
            log.info("Prompt search completed. totalHits={}", total);
            return new PromptSearchResult(total, prompts);

        } catch (OpenSearchException e) {
            logAppender.appendErrorLog(e);
            throw new IllegalStateException("Failed to execute OpenSearch All query", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute OpenSearch query", e);
        }
    }

    public PromptSearchResult search(PromptSearchCommand command) {
        SearchRequest request = buildSearchRequest(command);
        log.info("Executing prompt search. request={}", serializePlainJson(request));
        if (request.query() != null) {
            log.debug("Executing prompt search. query={}", serializePlainJson(request.query()));
        }

        try {
            SearchResponse<PromptDocument> response = openSearchClient.search(request, PromptDocument.class);

            List<PromptSummary> prompts = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(this::toSummary)
                    .toList();

            long total = extractTotalHits(response.hits().total(), prompts.size());
            log.info("Prompt search completed. totalHits={}", total);
            return new PromptSearchResult(total, prompts);
        } catch (OpenSearchException e) {
            logAppender.appendErrorLog(e);
            throw new IllegalStateException("Failed to execute OpenSearch query: %s".formatted(command.searchText()), e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute OpenSearch query", e);
        }
    }

    private static long extractTotalHits(TotalHits totalHits, int fallback) {
        return (totalHits == null) ? fallback : totalHits.value();
    }

    private SearchRequest buildSearchRequest(PromptSearchCommand command) {
        // OR 그룹(should) + AND 그룹(filter[=기간])
        List<Query> shouldQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        String searchText = command.searchText();

        // term 으로 정확히 일치하는 요소들에 대해 검색 - OR
        addShouldForKeyword(shouldQueries, TAGS_FIELD, searchText);
        addShouldForKeyword(shouldQueries, CATEGORY_FIELD, searchText);
        addShouldForKeyword(shouldQueries, PARENT_CATEGORY_FIELD, searchText);

        // payload 는 match 로 검색 - OR
        if (StringUtils.hasText(searchText)) {
            shouldQueries.add(Query.of(q -> q.match(m -> m.field(PAYLOAD_FIELD).query(FieldValue.of(searchText)))));
        }

        // AND: 기간 필터
        if (command.from() != null || command.to() != null) {
            filterQueries.add(buildRangeFilter(command.from(), command.to()));
        }

        Query rootQuery = constructRootQuery(shouldQueries, filterQueries);

        return SearchRequest.of(sr -> sr
                .index(INDEX_NAME)
                .query(rootQuery)
                .sort(s -> s.field(f -> f.field(CREATED_AT_FIELD).order(SortOrder.Desc))));
    }

    /**
     * should(OR) + minimum_should_match(1) + filter(AND) 조합
     */
    private static Query constructRootQuery(List<Query> shouldQueries, List<Query> filterQueries) {
        return Query.of(q -> q.bool(b -> {
            if (!shouldQueries.isEmpty()) {
                b.should(shouldQueries).minimumShouldMatch("1");
            }
            if (!filterQueries.isEmpty()) {
                b.filter(filterQueries);
            }
            return b;
        }));
    }

    /**
     * createdAt range: epoch_millis로 포맷 명시
     */
    private Query buildRangeFilter(Instant from, Instant to) {
        return Query.of(q -> q.range(r -> {
            r.field(CREATED_AT_FIELD);
            r.format("epoch_millis");
            if (from != null) {
                r.gte(JsonData.of(from.toEpochMilli()));
            }
            if (to != null) {
                r.lte(JsonData.of(to.toEpochMilli()));
            }
            return r;
        }));
    }

    /**
     * keyword(정확 일치)가 맞는 필드에 OR 조건 추가
     */
    private void addShouldForKeyword(List<Query> shoulds, String field, String value) {
        if (StringUtils.hasText(value)) {
            shoulds.add(Query.of(q -> q.term(t -> t.field(field).value(FieldValue.of(value)))));
        }
    }

    private String serializePlainJson(PlainJsonSerializable serializable) {
        if (serializable == null) {
            return "";
        }

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper);
            try (JsonGenerator generator = mapper.jsonProvider().createGenerator(buffer)) {
                serializable.serialize(generator, mapper);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to serialize {} for logging. error={}", serializable.getClass().getSimpleName(), e.getMessage());
            return "<serialization-error>";
        }
    }

    private PromptSummary toSummary(PromptDocument document) {
        List<String> tags = document.getTags() == null ? List.of() : document.getTags();
        return new PromptSummary(
                document.getId(),
                document.getPromptId(),
                document.getMessageId(),
                document.getStatus(),
                document.getProvider(),
                document.getCategory(),
                document.getParentCategory(),
                tags,
                document.getCreatedAt(),
                document.getAnalyzedAt(),
                document.getPayload(),
                document.getError()
        );
    }

    private static String resolveIndexName() {
        Document annotation = PromptDocument.class.getAnnotation(Document.class);
        if (annotation == null || !StringUtils.hasText(annotation.indexName())) {
            throw new IllegalStateException("PromptDocument must declare an indexName");
        }
        return annotation.indexName();
    }
}
