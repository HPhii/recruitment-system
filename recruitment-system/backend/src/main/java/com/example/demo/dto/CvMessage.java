package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CvMessage {
    @JsonProperty("filePath")
    private String filePath;

    @JsonProperty("fileName")
    private String fileName;

    public CvMessage() {}
}