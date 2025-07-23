package com.example.demo.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchFilters {
     //Dùng để truyền giá trị tìm kiếm từ client lên Elasticsearch
    String term;
}
