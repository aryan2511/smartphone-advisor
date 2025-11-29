package com.phonepick.advisor.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phonepick.advisor.service.FlipkartDataParser;
import com.phonepick.advisor.service.TranscriptBatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin controller for managing batch processing jobs
 */
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingController {
    
    private final TranscriptBatchService batchService;
    private final FlipkartDataParser flipkartDataParser;
    
    /**
     * Manually trigger batch processing
     * POST /api/admin/batch/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> triggerBatchProcessing() {
        try {
            log.info("Manual batch processing triggered");
            String result = batchService.processAllPending();
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            log.error("Batch processing failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Batch processing failed"));
        }
    }
    
    /**
     * Update phone aggregate scores
     * POST /api/admin/batch/update-scores
     */
    @PostMapping("/update-scores")
    public ResponseEntity<?> updateScores() {
        try {
            log.info("Manual score update triggered");
            batchService.updatePhoneAggregateScores();
            return ResponseEntity.ok(Map.of("message", "Scores updated successfully"));
        } catch (Exception e) {
            log.error("Score update failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Score update failed"));
        }
    }
    
    /**
     * Import Flipkart scraped data
     * POST /api/admin/batch/import-flipkart
     */
    @PostMapping("/import-flipkart")
    public ResponseEntity<?> importFlipkartData() {
        try {
            log.info("Flipkart data import triggered");
            String csvPath = "D:\\phone-pick-helper\\backend\\src\\main\\resources\\android_smartphones.csv";
            flipkartDataParser.parseAndSaveFlipkartData(csvPath);
            return ResponseEntity.ok(Map.of("message", "Flipkart data imported successfully"));
        } catch (Exception e) {
            log.error("Flipkart import failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Import failed: " + e.getMessage()));
        }
    }
}
