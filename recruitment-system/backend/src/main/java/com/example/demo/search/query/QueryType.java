package com.example.demo.search.query;

public enum QueryType {
    // Enum để định nghĩa loại truy vấn Elasticsearch có thể thực hiện
    MATCH,
    PREFIX,
    NESTED_PREFIX // ví dụ dùng cho nested query tìm theo kỹ năng
}

