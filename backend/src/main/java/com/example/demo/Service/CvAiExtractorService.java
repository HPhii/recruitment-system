package com.example.demo.Service;

import com.example.demo.entity.Cv;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class CvAiExtractorService {

    @Value("${nvidia.api.key:}")
    private String apiKey;

    private static final String API_ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Cv extractCvEntityFromText(String cvText) {
        if (cvText == null || cvText.trim().isEmpty()) {
            throw new IllegalArgumentException("CV text cannot be null or empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("NVIDIA API key is not configured");
            }
            headers.setBearerAuth(apiKey);

            String prompt = """
                    Extract the following fields from the CV and return ONLY the raw JSON object with this structure:
                                    {
                                      "fullName": "",
                                      "email": "",
                                      "phone": "",
                                      "education": "",
                                      "experience": "",
                                      "skills": [""]
                                    }
                                    Do NOT include any additional text, explanations, or reasoning, including <think> tags or comments. Return the JSON object alone.
                                    CV Content:
                """ + cvText.trim();

            log.info("Sending prompt to AI: {}", prompt);

            var body = Map.of(
                    "model", "nvidia/llama-3.1-nemotron-ultra-253b-v1",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.6,
                    "max_tokens", 4096
            );

            HttpEntity<?> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_ENDPOINT, request, String.class);

            if (response.getStatusCode().isError()) {
                log.error("API returned error: {} - {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("API error: " + response.getStatusCode() + " - " + response.getBody());
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Received empty or null response from AI API");
            }

            if (responseBody.trim().startsWith("<")) {
                log.error("Received HTML response instead of JSON: {}", responseBody);
                throw new RuntimeException("API returned HTML instead of JSON");
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody)
                    .path("choices").get(0)
                    .path("message")
                    .path("content");

            if (jsonNode.isMissingNode() || jsonNode.isNull()) {
                throw new RuntimeException("Invalid or missing content in AI response");
            }

            String content = jsonNode.asText();
            log.info("AI Response Content: {}", content);

            JsonNode dataNode;
            try {
                dataNode = objectMapper.readTree(content);
            } catch (Exception e) {
                log.error("Failed to parse AI response as JSON: {}", content, e);
                throw new RuntimeException("Failed to parse AI response as JSON", e);
            }

            return Cv.builder()
                    .fullName(getTextOrEmpty(dataNode, "fullName"))
                    .email(getTextOrEmpty(dataNode, "email"))
                    .phone(getTextOrEmpty(dataNode, "phone"))
                    .education(getTextOrEmpty(dataNode, "education"))
                    .experience(getTextOrEmpty(dataNode, "experience"))
                    .extractedSkills(extractSkills(dataNode))
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("API error while extracting CV: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to connect to AI API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error extracting CV using AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract CV using AI", e);
        }
    }

    private String getTextOrEmpty(JsonNode node, String field) {
        return node.path(field).isMissingNode() ? "" : node.path(field).asText();
    }

    private List<String> extractSkills(JsonNode node) {
        return StreamSupport.stream(node.path("skills").spliterator(), false)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .toList();
    }
}