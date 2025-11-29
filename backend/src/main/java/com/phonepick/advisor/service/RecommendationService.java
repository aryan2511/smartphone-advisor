package com.phonepick.advisor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.model.PhoneInsight;
import com.phonepick.advisor.model.PhoneRecommendation;
import com.phonepick.advisor.model.RecommendationRequest;
import com.phonepick.advisor.repository.PhoneInsightRepository;
import com.phonepick.advisor.repository.PhoneRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final PhoneRepository phoneRepository;
    private final PhoneInsightRepository phoneInsightRepository;
    private final UnifiedScoringService unifiedScoringService;
    // private final RedditService redditService;
    
    /**
     * Main recommendation algorithm
     * Finds phones in budget range and ranks them by weighted score
     */
    public List<PhoneRecommendation> getRecommendations(RecommendationRequest request) {
        log.info("Getting recommendations for budget: {} with priorities: {}", 
                 request.getBudget(), request.getPriorities());
        
        // Get budget range
        RecommendationRequest.BudgetRange budgetRange = request.getBudgetRange();
        
        // Find phones in budget range
        List<Phone> phonesInBudget = phoneRepository.findByPriceBetween(
            budgetRange.getMin(), 
            budgetRange.getMax()
        );
        
        log.info("Found {} phones in budget range: ₹{} - ₹{}", 
                 phonesInBudget.size(), budgetRange.getMin(), budgetRange.getMax());
        
        // Build priority pattern from user's priorities
        String priorityPattern = buildPriorityPattern(request.getPriorities());
        log.info("Priority pattern: {}", priorityPattern);
        
        // Get top 2 priorities for comparison logic
        List<String> topPriorities = getTopPriorities(request.getPriorities(), 2);
        
        // Calculate weighted scores and convert to recommendations
        List<PhoneRecommendation> recommendations = phonesInBudget.stream()
            .collect(Collectors.toMap(
                Phone::getId, // Key by phone ID
                phone -> phone, // Value is the phone itself
                (existing, replacement) -> existing // Keep first if duplicate IDs (shouldn't happen)
            ))
            .values().stream() // Now we have unique phones by ID
            .map(phone -> {
                // Calculate base match score from user priorities
                int baseMatchScore = calculateMatchScore(phone, request.getPriorities());
                
                // Calculate unified score incorporating YouTube, Reddit, and spec scores
                int unifiedScore = unifiedScoringService.calculateUnifiedScore(phone, null);
                
                // Combine priority match with unified scoring (70% priority match, 30% unified)
                int finalMatchScore = (int) Math.round(baseMatchScore * 0.70 + unifiedScore * 0.30);
                
                log.debug("Phone {}: base={}, unified={}, final={}",
                         phone.getModel(), baseMatchScore, unifiedScore, finalMatchScore);
                
                PhoneRecommendation recommendation = PhoneRecommendation.fromPhone(phone, finalMatchScore);
                
                // Fetch and add insights
                addInsightsToRecommendation(recommendation, phone.getId(), priorityPattern);
                
                return recommendation;
            })
            .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore())) // Sort by match score descending
            .collect(Collectors.groupingBy(
                rec -> rec.getBrand() + "::" + rec.getModel() // Group by brand + model
            ))
            .values().stream()
            .map(list -> list.get(0)) // Take only the first (highest scored) variant of each model
            .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore())) // Re-sort after deduplication
            .limit(5) // Return top 5 unique phone models
            .collect(Collectors.toList());
        
        // Generate dynamic comparisons for top recommendation
        if (recommendations.size() > 1) {
            PhoneRecommendation topPick = recommendations.get(0);
            generateDynamicComparisons(topPick, recommendations, phonesInBudget, topPriorities);
        }
        
        log.info("Returning {} recommendations with insights", recommendations.size());
        
        return recommendations;
    }
    
    /**
     * Get top N priorities sorted by weight
     */
    private List<String> getTopPriorities(Map<String, Integer> priorities, int count) {
        return priorities.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(count)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate dynamic comparisons between top pick and alternatives
     */
    private void generateDynamicComparisons(PhoneRecommendation topPick, 
                                           List<PhoneRecommendation> allRecommendations,
                                           List<Phone> allPhones,
                                           List<String> topPriorities) {
        // Get the actual Phone objects for comparison
        Phone topPhone = allPhones.stream()
            .filter(p -> p.getId().equals(topPick.getId()))
            .findFirst()
            .orElse(null);
        
        if (topPhone == null) return;
        
        // Compare with #2 and #3 (if they exist)
        for (int i = 1; i < Math.min(3, allRecommendations.size()); i++) {
            PhoneRecommendation alternative = allRecommendations.get(i);
            
            Phone altPhone = allPhones.stream()
                .filter(p -> p.getId().equals(alternative.getId()))
                .findFirst()
                .orElse(null);
            
            if (altPhone != null) {
                String comparisonReason = generateComparisonReason(topPhone, altPhone, topPriorities);
                topPick.addAlternativeComparison(
                    alternative.getBrand(),
                    alternative.getModel(),
                    comparisonReason
                );
            }
        }
    }
    
    /**
     * Generate comparison reason based on feature scores
     */
    private String generateComparisonReason(Phone topPhone, Phone alternative, List<String> topPriorities) {
        List<String> advantages = new ArrayList<>();
        
        // Check user's top priorities
        for (String priority : topPriorities) {
            int topScore = getFeatureScore(topPhone, priority);
            int altScore = getFeatureScore(alternative, priority);
            
            if (topScore > altScore) {
                int diff = topScore - altScore;
                if (diff >= 5) { // Only mention if significantly better
                    String advantage = getAdvantageText(priority, diff);
                    if (advantage != null) {
                        advantages.add(advantage);
                    }
                }
            }
        }
        
        // Check price advantage
        if (topPhone.getPrice() < alternative.getPrice()) {
            int savings = alternative.getPrice() - topPhone.getPrice();
            advantages.add("₹" + String.format("%,d", savings) + " cheaper");
        }
        
        // If no clear advantages found, use generic text
        if (advantages.isEmpty()) {
            return "Better overall balance for your priorities";
        }
        
        // Join advantages
        if (advantages.size() == 1) {
            return advantages.get(0);
        } else if (advantages.size() == 2) {
            return advantages.get(0) + " and " + advantages.get(1);
        } else {
            return advantages.get(0) + ", " + advantages.get(1) + ", and more";
        }
    }
    
    /**
     * Get feature score by name
     */
    private int getFeatureScore(Phone phone, String feature) {
        return switch (feature.toLowerCase()) {
            case "camera" -> phone.getCameraScore();
            case "battery" -> phone.getBatteryScore();
            case "performance", "software" -> phone.getSoftwareScore();
            case "privacy" -> phone.getPrivacyScore();
            case "looks", "design" -> phone.getLooksScore();
            default -> 0;
        };
    }
    
    /**
     * Convert feature advantage to human-readable text
     */
    private String getAdvantageText(String feature, int scoreDiff) {
        return switch (feature.toLowerCase()) {
            case "camera" -> scoreDiff >= 10 ? "significantly better camera" : "better camera quality";
            case "battery" -> scoreDiff >= 10 ? "much longer battery life" : "better battery life";
            case "performance", "software" -> scoreDiff >= 10 ? "noticeably faster performance" : "smoother performance";
            case "privacy" -> scoreDiff >= 10 ? "stronger privacy protection" : "better privacy features";
            case "looks", "design" -> scoreDiff >= 10 ? "premium design and build" : "better design";
            default -> null;
        };
    }
    
    /**
     * Build priority pattern string from user priorities
     * Format: "camera-battery-performance-privacy-design"
     */
    private String buildPriorityPattern(Map<String, Integer> priorities) {
        return priorities.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort by value descending
            .map(Map.Entry::getKey)
            .collect(Collectors.joining("-"));
    }
    
    /**
     * Fetch insights for a phone and add to recommendation
     */
    private void addInsightsToRecommendation(PhoneRecommendation recommendation, Long phoneId, String priorityPattern) {
        try {
            // Try exact match first
            PhoneInsight insight = phoneInsightRepository
                .findByPhoneIdAndPriorityPattern(phoneId, priorityPattern)
                .orElse(null);
            
            // If no exact match, get ANY insight for this phone (fallback)
            if (insight == null) {
                insight = phoneInsightRepository
                    .findFirstByPhoneId(phoneId)
                    .orElse(null);
                
                if (insight != null) {
                    log.debug("Using fallback insight for phone {} (exact pattern not found)", phoneId);
                }
            } else {
                log.debug("Found exact insight match for phone {} with pattern {}", phoneId, priorityPattern);
            }
            
            if (insight != null) {
                recommendation.addInsights(insight);
            } else {
                log.warn("No insights found at all for phone {}", phoneId);
            }
        } catch (Exception e) {
            log.error("Error fetching insights for phone {}: {}", phoneId, e.getMessage());
            // Continue without insights rather than failing
        }
    }
    
    /**
     * Calculate weighted match score for a phone based on user priorities
     * Formula: Sum of (feature_score * priority_weight) / number_of_features
     */
    private int calculateMatchScore(Phone phone, Map<String, Integer> priorities) {
        int totalScore = 0;
        int totalWeight = 0;
        
        // Camera
        int cameraWeight = priorities.getOrDefault("camera", 50);
        totalScore += phone.getCameraScore() * cameraWeight / 100;
        totalWeight += cameraWeight;
        
        // Battery
        int batteryWeight = priorities.getOrDefault("battery", 50);
        totalScore += phone.getBatteryScore() * batteryWeight / 100;
        totalWeight += batteryWeight;
        
        // Software/Performance
        int performanceWeight = priorities.getOrDefault("performance", 50);
        totalScore += phone.getSoftwareScore() * performanceWeight / 100;
        totalWeight += performanceWeight;
        
        // Privacy
        int privacyWeight = priorities.getOrDefault("privacy", 50);
        totalScore += phone.getPrivacyScore() * privacyWeight / 100;
        totalWeight += privacyWeight;
        
        // Looks/Design
        int looksWeight = priorities.getOrDefault("looks", 50);
        totalScore += phone.getLooksScore() * looksWeight / 100;
        totalWeight += looksWeight;
        
        // Normalize to 0-100 scale
        return Math.round((float) totalScore / 5);
    }
    
    /**
     * Get all phones (for testing/admin purposes)
     */
    public List<Phone> getAllPhones() {
        return phoneRepository.findAll();
    }
    
    /**
     * Get phone count in a budget range
     */
    public Long getPhoneCountInBudget(String budget) {
        RecommendationRequest request = new RecommendationRequest();
        request.setBudget(budget);
        RecommendationRequest.BudgetRange range = request.getBudgetRange();
        return phoneRepository.countByPriceBetween(range.getMin(), range.getMax());
    }
}
