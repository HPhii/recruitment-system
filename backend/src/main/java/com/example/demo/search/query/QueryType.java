package com.example.demo.search.query;

public enum QueryType {
    // Enum để định nghĩa loại truy vấn Elasticsearch có thể thực hiện
    MATCH,
    PREFIX,
    NESTED_PREFIX,
    NESTED_MULTI_MATCH_PREFIX // Thêm loại truy vấn mới
}