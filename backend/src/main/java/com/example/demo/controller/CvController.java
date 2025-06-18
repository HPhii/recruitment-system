package com.example.demo.controller;

import com.example.demo.Service.CvAiExtractorService;
import com.example.demo.Service.CvService;
import com.example.demo.entity.Cv;
import com.example.demo.repository.CvRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CvController {

    private final CvRepository cvRepository;
    private final CvService cvService;
    private final CvAiExtractorService cvAiExtractorService;


//        @PostMapping("/upload")
//        public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file) throws IOException {
//            // 1. Trích xuất text từ file
//            String text = cvService.extractTextFromFile(file);
//
//            // 2. Parse thông tin từ text
//            Cv cv = cvService.parseCv(text, file.getOriginalFilename());
//
//            // 3. Lưu vào database
//            cvRepository.save(cv);
//
//            return ResponseEntity.ok("CV uploaded and saved successfully.");
//        }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file) throws IOException {
        String text = cvService.extractTextFromFile(file);
        Cv cv = cvAiExtractorService.extractCvEntityFromText(text);
        cv.setFileName(file.getOriginalFilename());
        cvRepository.save(cv);
        return ResponseEntity.ok("CV uploaded and saved successfully.");
    }
    }




