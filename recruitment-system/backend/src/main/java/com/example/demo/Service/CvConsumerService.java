package com.example.demo.Service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.CvMessage;
import com.example.demo.entity.Cv;
import com.example.demo.repository.CvRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CvConsumerService {

    private final CvService cvService;

    private final CvAiExtractorService cvAiExtractorService;

    private final CvRepository cvRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, concurrency = "5")
    public void processCv(CvMessage cvMessage ){
        try {
            File file = new File(cvMessage.getFilePath());
            if (!file.exists()) {
                throw new IOException("File not found: " + cvMessage.getFilePath());
            }
            String text = cvService.extractTextFromFile(file);
            Cv cv = cvAiExtractorService.extractCvEntityFromText(text);
            cv.setFileName(cvMessage.getFileName());
            cvRepository.save(cv);
            if (!file.delete()) {
                System.err.println("Failed to delete file: " + cvMessage.getFilePath());
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + cvMessage.getFileName() + " - " + e.getMessage());
        }
    }
}