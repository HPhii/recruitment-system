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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CvService {

    public String extractTextFromFile(MultipartFile file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();

        if (extension.equals("pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if (extension.equals("docx")) {
            XWPFDocument doc = new XWPFDocument(file.getInputStream());
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return extractor.getText();
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    public Cv parseCv(String text, String fileName) {
        String fullName = extractFullName(text);
        String email = extractEmail(text);
        String phone = extractPhone(text);
        List<String> skills = extractSkills(text);
        String education = extractEducation(text);
        String experience = extractExperience(text);

        return Cv.builder()
                .fileName(fileName)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .education(education)
                .experience(experience)
                .extractedSkills(skills)
                .build();
    }

    // Giản lược, bạn có thể cải tiến sau
    private String extractEmail(String text) {
        Matcher matcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,6}").matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractPhone(String text) {
        Matcher matcher = Pattern.compile("(\\+?\\d{1,3})?\\s?(\\(?\\d{2,4}\\)?)?[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}").matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractFullName(String text) {
        String[] lines = text.split("\n");
        return lines.length > 0 ? lines[0].trim() : null;
    }

    private List<String> extractSkills(String text) {
        List<String> knownSkills = List.of("Java", "Spring Boot", "Python", "SQL", "React", "Docker");
        String lowerText = text.toLowerCase();

        return knownSkills.stream()
                .filter(skill -> lowerText.contains(skill.toLowerCase()))
                .toList();
    }

    private String extractEducation(String text) {
        List<String> eduKeywords = List.of("university", "bachelor", "master", "degree", "education");
        return Arrays.stream(text.split("\n"))
                .filter(line -> eduKeywords.stream().anyMatch(line.toLowerCase()::contains))
                .collect(Collectors.joining("\n")).trim();
    }

    private String extractExperience(String text) {
        List<String> expKeywords = List.of("developer", "engineer", "company", "experience", "worked", "project", "solutions");
        return Arrays.stream(text.split("\n"))
                .filter(line -> expKeywords.stream().anyMatch(line.toLowerCase()::contains))
                .collect(Collectors.joining("\n")).trim();
    }

}
