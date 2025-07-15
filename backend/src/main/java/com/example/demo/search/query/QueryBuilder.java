package com.example.demo.search.query;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.example.demo.search.SearchFilters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class QueryBuilder {

    public static SearchRequest buildSearchRequest(final SearchFilters filters, final SearchMeta meta) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(meta.getIndex());

        Query query = buildQuery(filters, meta);
        builder.query(query);

        return builder.build();
    }

    private static Query buildQuery(SearchFilters filters, SearchMeta meta) {
        return switch (meta.getType()) {
            case MATCH -> buildMatchQuery(meta.getFields(), filters.getTerm());
            case PREFIX -> buildPrefixQuery(meta.getFields(), filters.getTerm());
            case NESTED_PREFIX -> buildNestedPrefixQuery(meta.getFields().get(0), filters.getTerm());
            case NESTED_MULTI_MATCH_PREFIX -> buildNestedMultiMatchPrefixQuery(meta.getFields().get(0), filters.getTerms());
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

    private static Query buildNestedPrefixQuery(String field, String value) {
        Query prefixQuery = new Query.Builder()
                .prefix(p -> p
                        .field(field)
                        .value(value)
                        .caseInsensitive(true)
                ).build();

        return new Query.Builder()
                .nested(n -> n
                        .path("skills")
                        .query(prefixQuery))
                .build();
    }

    private static Query buildNestedMultiMatchPrefixQuery(String field, List<String> values) {
        List<Query> shouldQueries = new ArrayList<>();
        for (String value : values) {
            shouldQueries.add(
                    Query.of(q -> q
                            .prefix(p -> p
                                    .field(field)
                                    .value(value)
                                    .caseInsensitive(true)
                            )
                    )
            );
        }

        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .should(shouldQueries)
                        .minimumShouldMatch("1")
                )
        );

        return new Query.Builder()
                .nested(n -> n
                        .path("skills")
                        .query(boolQuery))
                .build();
    }
}