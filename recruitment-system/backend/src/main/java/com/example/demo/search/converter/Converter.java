package com.example.demo.search.converter;


public interface Converter <E, T > {
    Class<E> getDocumentClass();
    T convertToDto(E document);
    E convertToDocument(T dto);
}
