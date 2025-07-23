package com.example.demo.Service;

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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CvService {

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