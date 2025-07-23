package com.example.demo.search.query;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.demo.search.SearchFilters;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class QueryBuilder {
    // * Xây dựng SearchRequest từ SearchFilters và SearchMeta
    public static SearchRequest buildSearchRequest(final SearchFilters filters, final SearchMeta meta) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(meta.getIndex());

        Query query = buildQuery(filters, meta);
        builder.query(query);

        return builder.build();
    }
    private static Query buildQuery(SearchFilters filters, SearchMeta meta) {
        String term = filters.getTerm();

        return switch (meta.getType()) {
            case MATCH -> buildMatchQuery(meta.getFields(), term);
            case PREFIX -> buildPrefixQuery(meta.getFields(), term);
            case NESTED_PREFIX -> buildNestedPrefixQuery(meta.getFields(), term);
        };
    }
    private static Query buildMatchQuery(List<String> fields, String value) {
        return new Query.Builder()
                .match(new MatchQuery.Builder()
                        .field(fields.get(0))
                        .query(value)
                        .build())
                .build();
    }
    private static Query buildPrefixQuery(List<String> fields, String value) {
        return new Query.Builder()
                .prefix(new PrefixQuery.Builder()
                        .field(fields.get(0))
                        .value(value)
                        .caseInsensitive(true)
                        .build())
                .build();
    }

    private static Query buildNestedPrefixQuery(List<String> fields, String value) {
        List<Query> shouldQueries = new ArrayList<>();
        for (String field : fields) {
            shouldQueries.add(new Query.Builder()
                    .prefix(new PrefixQuery.Builder()
                            .field(field)
                            .value(value)
                            .caseInsensitive(true)
                            .build())
                    .build());
        }

        BoolQuery boolQuery = new BoolQuery.Builder()
                .should(shouldQueries)
                .minimumShouldMatch("1")
                .build();

        return new Query.Builder()
                .nested(n -> n
                        .path("skills")
                        .query(new Query.Builder().bool(boolQuery).build()))
                .build();
    }
}
