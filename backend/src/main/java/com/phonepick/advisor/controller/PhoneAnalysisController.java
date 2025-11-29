package com.phonepick.advisor.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phonepick.advisor.repository.PhoneRepository;
import com.phonepick.advisor.service.FlipkartDataParser;
import com.phonepick.advisor.service.PhoneAnalysisService;
import com.phonepick.advisor.service.PhoneScoreUpdateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for phone analysis endpoints
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class PhoneAnalysisController {
    
    private final PhoneAnalysisService phoneAnalysisService;
    private final FlipkartDataParser flipkartDataParser;
    private final PhoneRepository phoneRepository;
    private final PhoneScoreUpdateService phoneScoreUpdateService;
    
    /**
     * Analyze a single phone
     * POST /api/analysis/phone/{phoneId}
     */
    @PostMapping("/phone/{phoneId}")
    public ResponseEntity<?> analyzePhone(@PathVariable Long phoneId) {
        try {
            log.info("Received request to analyze phone: {}", phoneId);
            PhoneAnalysisService.PhoneAnalysisResult result = phoneAnalysisService.analyzePhone(phoneId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error analyzing phone {}: {}", phoneId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze phone"));
        }
    }
    
    /**
     * Batch analyze multiple phones
     * POST /api/analysis/phones
     * Body: [1, 2, 3, 4, 5]
     */
    @PostMapping("/phones")
    public ResponseEntity<?> analyzePhones(@RequestBody List<Long> phoneIds) {
        try {
            log.info("Received request to analyze {} phones", phoneIds.size());
            Map<Long, PhoneAnalysisService.PhoneAnalysisResult> results = 
                phoneAnalysisService.analyzePhones(phoneIds);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error analyzing phones: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze phones"));
        }
    }
    
    /**
     * Test endpoint
     * GET /api/analysis/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of(
            "status", "Controller is working",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Check database status
     * GET /api/analysis/db-status
     */
    @GetMapping("/db-status")
    public ResponseEntity<?> checkDatabaseStatus() {
        try {
            long count = phoneRepository.count();
            Map<String, Object> response = new HashMap<>();
            response.put("totalPhones", count);
            response.put("databaseConnected", true);
            
            if (count > 0) {
                // Get a sample
                var sample = phoneRepository.findAll().stream().limit(5).toList();
                response.put("sampleData", sample);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Database error", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Database error: " + e.getMessage(),
                    "databaseConnected", false
                ));
        }
    }
    
    /**
     * Check CSV file
     * GET /api/analysis/check-csv
     */
    @GetMapping("/check-csv")
    public ResponseEntity<?> checkCsvFile() {
        String csvPath = "D:\\phone-pick-helper\\android_smartphones.csv";
        File file = new File(csvPath);
        
        Map<String, Object> response = new HashMap<>();
        response.put("path", csvPath);
        response.put("exists", file.exists());
        response.put("canRead", file.canRead());
        response.put("size", file.length());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Clear all phone data (use with caution!)
     * DELETE /api/analysis/clear-phones
     */
    @DeleteMapping("/clear-phones")
    public ResponseEntity<?> clearPhones() {
        try {
            long count = phoneRepository.count();
            phoneRepository.deleteAll();
            log.warn("Deleted {} phones from database", count);
            
            return ResponseEntity.ok(Map.of(
                "message", "All phones deleted",
                "deletedCount", count
            ));
        } catch (Exception e) {
            log.error("Error clearing phones", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to clear phones: " + e.getMessage()));
        }
    }
    
    /**
     * Update all phone scores with enhanced scoring
     * POST /api/analysis/update-scores
     */
    @PostMapping("/update-scores")
    public ResponseEntity<?> updateAllPhoneScores() {
        try {
            log.info("Starting phone score update...");
            PhoneScoreUpdateService.UpdateResult result = phoneScoreUpdateService.updateAllPhoneScores();
            
            return ResponseEntity.ok(Map.of(
                "message", "Phone scores updated successfully",
                "updated", result.getUpdated(),
                "failed", result.getFailed(),
                "total", result.getUpdated() + result.getFailed()
            ));
        } catch (Exception e) {
            log.error("Error updating phone scores", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update scores: " + e.getMessage()));
        }
    }
    
    /**
     * Update a single phone's scores
     * POST /api/analysis/update-score/{phoneId}
     */
    @PostMapping("/update-score/{phoneId}")
    public ResponseEntity<?> updatePhoneScore(@PathVariable Long phoneId) {
        try {
            var phone = phoneScoreUpdateService.updatePhoneById(phoneId);
            return ResponseEntity.ok(Map.of(
                "message", "Phone scores updated",
                "phone", phone
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating phone score", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update score"));
        }
    }
    
    /**
     * Import Flipkart data
     * POST /api/analysis/import-flipkart
     */
    @PostMapping("/import-flipkart")
    public ResponseEntity<?> importFlipkartData() {
        try {
            log.info("Starting Flipkart data import...");
            String csvPath = "D:\\phone-pick-helper\\android_smartphones.csv";
            
            File file = new File(csvPath);
            if (!file.exists()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "CSV file not found at: " + csvPath));
            }
            
            long beforeCount = phoneRepository.count();
            Map<String, Object> stats = flipkartDataParser.parseAndSaveFlipkartData(csvPath);
            long afterCount = phoneRepository.count();
            
            Map<String, Object> response = new HashMap<>(stats);
            response.put("message", "Flipkart data import completed");
            response.put("phonesBeforeImport", beforeCount);
            response.put("phonesAfterImport", afterCount);
            response.put("phonesAdded", afterCount - beforeCount);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error importing Flipkart data", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Import failed: " + e.getMessage(),
                    "type", e.getClass().getName()
                ));
        }
    }
}
