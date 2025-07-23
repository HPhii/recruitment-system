package com.example.demo.controller;

import com.example.demo.Service.CvAiExtractorService;
import com.example.demo.Service.CvService;
import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.CvMessage;
import com.example.demo.entity.Cv;
import com.example.demo.repository.CvRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CvController {

    private final RabbitTemplate rabbitTemplate;
    private final CvRepository cvRepository;
    private final CvService cvService;
    private final CvAiExtractorService cvAiExtractorService;
    private static final String UPLOAD_DIR = "uploads/";

        @PostMapping("/upload")
        public ResponseEntity<String> uploadCv(@RequestParam("files") MultipartFile[] files) throws IOException {
            List<String> queuedFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = file.getOriginalFilename();
                    Path filePath = Paths.get(UPLOAD_DIR + fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, file.getBytes());
                    CvMessage cvMessage = new CvMessage();
                    cvMessage.setFilePath(filePath.toString());
                    cvMessage.setFileName(fileName);
                    System.out.println("Sending message (JSON): " + new ObjectMapper().writeValueAsString(cvMessage));
                    Message message = rabbitTemplate.getMessageConverter().toMessage(cvMessage, new MessageProperties());
                    System.out.println("Sending raw message: " + new String(message.getBody(), StandardCharsets.UTF_8));
                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, cvMessage);
                    queuedFiles.add(fileName);
                }
            }
            return ResponseEntity.ok("Files " + String.join(", ", queuedFiles) + " queued for processing.");
        }
    }





