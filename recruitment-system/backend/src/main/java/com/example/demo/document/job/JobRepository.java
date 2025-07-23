package com.example.demo.document.job;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface JobRepository extends ElasticsearchRepository< JobDocument, Long > {
}
