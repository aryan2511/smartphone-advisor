package com.phonepick.advisor.controller;

import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.model.PhoneRecommendation;
import com.phonepick.advisor.model.RecommendationRequest;
import com.phonepick.advisor.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * Main endpoint: Get phone recommendations
     * POST /api/recommend
     */
    @PostMapping("/recommend")
    public ResponseEntity<List<PhoneRecommendation>> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        
        log.info("Received recommendation request: {}", request);
        
        List<PhoneRecommendation> recommendations = recommendationService.getRecommendations(request);
        
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Phone Advisor API",
            "version", "1.0.0"
        ));
    }
    
    /**
     * Get all phones (for testing/admin)
     * GET /api/phones
     */
    @GetMapping("/phones")
    public ResponseEntity<List<Phone>> getAllPhones() {
        List<Phone> phones = recommendationService.getAllPhones();
        return ResponseEntity.ok(phones);
    }
    
    /**
     * Get phone count in budget
     * GET /api/phones/count?budget=20-25
     */
    @GetMapping("/phones/count")
    public ResponseEntity<Map<String, Object>> getPhoneCount(
            @RequestParam(required = false) String budget) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (budget != null) {
            Long count = recommendationService.getPhoneCountInBudget(budget);
            response.put("count", count);
            response.put("budget", budget);
        } else {
            Long totalCount = (long) recommendationService.getAllPhones().size();
            response.put("count", totalCount);
        }
        
        return ResponseEntity.ok(response);
    }
}
