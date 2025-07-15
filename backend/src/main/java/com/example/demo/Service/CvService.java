package com.example.demo.service;

import com.example.demo.entity.Cv;
import com.example.demo.repository.CvRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CvService {

    private final CvRepository cvRepository;
    private final com.example.demo.Service.CvAiExtractorService cvAiExtractorService;
    private static final String UPLOAD_DIR = "uploads/";


    public Cv processAndSaveCv(MultipartFile multipartFile) throws IOException {
        // 1. Save the file temporarily
        String fileName = multipartFile.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, multipartFile.getBytes());
        File file = filePath.toFile();

        // 2. Extract text from the file
        String text = extractTextFromFile(file);

        // 3. Use AI service to extract entity
        Cv cv = cvAiExtractorService.extractCvEntityFromText(text);

        // 4. Set file-related info and save to DB
        cv.setFileName(fileName);
        cv.setFileUrl(filePath.toString()); // Or a public URL if you store it in a cloud service
        Cv savedCv = cvRepository.save(cv);

        // 5. Clean up the temporary file
        if (!file.delete()) {
            System.err.println("Failed to delete temporary file: " + filePath);
        }

        return savedCv;
    }


    // Thêm phương thức mới để đọc từ File
    public String extractTextFromFile(File file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        try (FileInputStream fis = new FileInputStream(file)) {
            if (extension.equals("pdf")) {
                try (PDDocument document = PDDocument.load(fis)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            } else if (extension.equals("docx")) {
                try (XWPFDocument doc = new XWPFDocument(fis)) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                    return extractor.getText();
                }
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + extension);
            }
        }
    }
}