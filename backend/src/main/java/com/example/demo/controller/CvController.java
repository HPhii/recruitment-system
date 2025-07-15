package com.example.demo.controller;

import com.example.demo.entity.Cv;
import com.example.demo.service.CvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CvController {

    private final CvService cvService;

    @PostMapping("/upload")
    public ResponseEntity<List<Cv>> uploadCv(@RequestParam("files") MultipartFile[] files) {
        List<Cv> processedCvs = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    Cv cv = cvService.processAndSaveCv(file);
                    processedCvs.add(cv);
                } catch (IOException e) {
                    log.error("Error processing file: " + file.getOriginalFilename(), e);
                    // Optionally, you can return an error response here
                    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        }
        return ResponseEntity.ok(processedCvs);
    }
}