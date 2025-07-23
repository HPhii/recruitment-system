package com.example.demo.search;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demo.search.converter.Converter;
import com.example.demo.search.query.QueryBuilder;
import com.example.demo.search.query.SearchMeta;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElasticsearchProxy<E , T > {
//    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchProxy.class);

    Map<Class<E>, Converter<E, T>> CONVERTER_MAP = new HashMap<>(10);
    ElasticsearchClient client;
    List<Converter<E, T>> converters;

    public ElasticsearchProxy(ElasticsearchClient client, List<Converter<E, T>> converters) {
        this.client = client;
        this.converters = converters;

        for (Converter<E, T> converter : converters) {
            CONVERTER_MAP.put(converter.getDocumentClass(), converter);
        }
    }

    public List<T> search(final SearchFilters filters, final SearchMeta meta, final Class<E> documentClass) {
        log.info("Executing search for index {}, fields {}, term {}", meta.getIndex(), meta.getFields(), filters.getTerm());
        try {
            SearchResponse<E> response = client.search(
                    QueryBuilder.buildSearchRequest(filters, meta),
                    documentClass
            );

            List<E> documents = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            Converter<E, T> converter = CONVERTER_MAP.get(documentClass);
            return documents.stream()
                    .map(converter::convertToDto)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Elasticsearch query failed: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
