package com.example.demo.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchMeta {
    //Dùng để cấu hình metadata cho truy vấn: gồm danh sách field, tên index và loại query
    List<String> fields;
    String index;
    QueryType type;
}
