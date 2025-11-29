package com.phonepick.advisor.service;

import com.phonepick.advisor.model.Phone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified scoring service that combines multiple data sources
 * - YouTube review sentiment (feature-based)
 * - Reddit community sentiment
 * - Our own specification scoring
 */
@Service
@Slf4j
public class UnifiedScoringService {
    
    // Weight configuration for different sources
    private static final double SPEC_WEIGHT = 0.50;        // 50% - Our spec analysis
    private static final double YOUTUBE_BASE_WEIGHT = 0.35; // 35% base - YouTube reviews
    private static final double REDDIT_WEIGHT = 0.15;      // 15% - Reddit sentiment
    
    // Bonus for strong YouTube consensus (3+ out of 5-6 reviewers recommend)
    private static final int YOUTUBE_CONSENSUS_BONUS = 3;  // +3 points bonus
    private static final int MIN_REVIEWERS_FOR_BONUS = 3;  // Need 3+ positive reviews
    private static final int CONSENSUS_THRESHOLD = 70;     // 70+ score = positive recommendation
    
    private final PhoneSpecScoringService specScoringService;
    
    public UnifiedScoringService(PhoneSpecScoringService specScoringService) {
        this.specScoringService = specScoringService;
    }
    
    /**
     * Calculate unified score for a phone combining all sources
     * @param phone Phone object with all specifications
     * @param youtubeReviewScores Map of channel name to sentiment score (optional)
     * @return Unified score (0-100)
     */
    public int calculateUnifiedScore(Phone phone, Map<String, Integer> youtubeReviewScores) {
        // Calculate spec-based score
        int specScore = calculateSpecScore(phone);
        
        // Get stored sentiment scores
        Integer youtubeScore = phone.getYoutubeSentimentScore();
        Integer redditScore = phone.getRedditSentimentScore();
        
        // If individual review scores provided, check for consensus bonus
        int youtubeBonus = 0;
        if (youtubeReviewScores != null && youtubeReviewScores.size() >= MIN_REVIEWERS_FOR_BONUS) {
            if (hasYouTubeConsensus(youtubeReviewScores)) {
                youtubeBonus = YOUTUBE_CONSENSUS_BONUS;
                log.info("YouTube consensus bonus applied for phone: {}", phone.getModel());
            }
        }
        
        // Calculate weighted score
        double finalScore = 0.0;
        double totalWeight = 0.0;
        
        // Spec score (always available)
        finalScore += specScore * SPEC_WEIGHT;
        totalWeight += SPEC_WEIGHT;
        
        // YouTube score (if available)
        if (youtubeScore != null && youtubeScore > 0) {
            finalScore += youtubeScore * YOUTUBE_BASE_WEIGHT;
            totalWeight += YOUTUBE_BASE_WEIGHT;
            
            // Add consensus bonus
            finalScore += youtubeBonus;
        }
        
        // Reddit score (if available)
        if (redditScore != null && redditScore > 0) {
            finalScore += redditScore * REDDIT_WEIGHT;
            totalWeight += REDDIT_WEIGHT;
        }
        
        // Normalize if we don't have all scores
        if (totalWeight < 1.0) {
            finalScore = finalScore / totalWeight;
        }
        
        int result = Math.max(0, Math.min(100, (int) Math.round(finalScore)));
        
        log.debug("Unified score for {}: spec={}, youtube={}, reddit={}, final={}",
                 phone.getModel(), specScore, youtubeScore, redditScore, result);
        
        return result;
    }
    
    /**
     * Calculate spec-based score (average of all feature scores)
     */
    private int calculateSpecScore(Phone phone) {
        int cameraScore = specScoringService.scoreCameraSpec(phone.getCameraInfo());
        int batteryScore = specScoringService.scoreBatterySpec(phone.getBattery());
        int storageScore = specScoringService.scoreStorageAndRam(phone.getMemory_and_storage());
        int processorScore = specScoringService.scoreProcessor(phone.getProcessor());
        int displayScore = specScoringService.scoreDisplay(phone.getDisplayInfo());
        
        // Average of all specs
        return (cameraScore + batteryScore + storageScore + processorScore + displayScore) / 5;
    }
    
    /**
     * Check if there's a strong consensus among YouTube reviewers
     * Consensus = at least 3 out of 5-6 reviewers gave 70+ score (positive recommendation)
     * This provides additional validation beyond individual sentiment scores
     */
    private boolean hasYouTubeConsensus(Map<String, Integer> reviewScores) {
        if (reviewScores == null || reviewScores.isEmpty()) {
            return false;
        }
        
        long highScoreCount = reviewScores.values().stream()
            .filter(score -> score >= CONSENSUS_THRESHOLD)
            .count();
        
        // Require absolute minimum of 3 positive reviews
        if (highScoreCount < MIN_REVIEWERS_FOR_BONUS) {
            return false;
        }
        
        // For 5-6 reviewers: need at least 3 positive (50%+)
        // For 3-4 reviewers: need all 3 positive (75%+)
        int totalReviewers = reviewScores.size();
        double consensusRatio = (double) highScoreCount / totalReviewers;
        
        boolean hasConsensus = totalReviewers >= 5 ? 
            consensusRatio >= 0.50 :  // 3/5 or 3/6 = 50%+
            consensusRatio >= 0.75;   // 3/4 = 75% or 3/3 = 100%
        
        if (hasConsensus) {
            log.info("YouTube consensus achieved: {}/{} reviewers positive ({}%)",
                    highScoreCount, totalReviewers, (int)(consensusRatio * 100));
        }
        
        return hasConsensus;
    }
    
    /**
     * Calculate feature-specific unified scores
     * Useful for detailed breakdowns
     */
    public Map<String, Integer> calculateFeatureScores(Phone phone, 
                                                       Map<String, Map<String, Integer>> youtubeFeatureScores) {
        Map<String, Integer> unifiedScores = new HashMap<>();
        
        // Camera
        int cameraSpec = specScoringService.scoreCameraSpec(phone.getCameraInfo());
        int cameraYT = getAverageFeatureScore(youtubeFeatureScores, "camera");
        unifiedScores.put("camera", combineScores(cameraSpec, cameraYT, phone.getYoutubeSentimentScore()));
        
        // Battery
        int batterySpec = specScoringService.scoreBatterySpec(phone.getBattery());
        int batteryYT = getAverageFeatureScore(youtubeFeatureScores, "battery");
        unifiedScores.put("battery", combineScores(batterySpec, batteryYT, phone.getYoutubeSentimentScore()));
        
        // Performance
        int perfSpec = specScoringService.scoreProcessor(phone.getProcessor());
        int perfYT = getAverageFeatureScore(youtubeFeatureScores, "performance");
        unifiedScores.put("performance", combineScores(perfSpec, perfYT, phone.getYoutubeSentimentScore()));
        
        // Display
        int displaySpec = specScoringService.scoreDisplay(phone.getDisplayInfo());
        int displayYT = getAverageFeatureScore(youtubeFeatureScores, "display");
        unifiedScores.put("display", combineScores(displaySpec, displayYT, phone.getYoutubeSentimentScore()));
        
        // Design (no separate spec scoring, use looks_score if available)
        int designScore = phone.getLooksScore() != null ? phone.getLooksScore() : 50;
        int designYT = getAverageFeatureScore(youtubeFeatureScores, "design");
        unifiedScores.put("design", combineScores(designScore, designYT, phone.getYoutubeSentimentScore()));
        
        return unifiedScores;
    }
    
    /**
     * Get average feature score from YouTube reviews
     */
    private int getAverageFeatureScore(Map<String, Map<String, Integer>> youtubeFeatureScores, 
                                      String feature) {
        if (youtubeFeatureScores == null || youtubeFeatureScores.isEmpty()) {
            return 0;
        }
        
        int total = 0;
        int count = 0;
        
        for (Map<String, Integer> channelScores : youtubeFeatureScores.values()) {
            if (channelScores.containsKey(feature)) {
                total += channelScores.get(feature);
                count++;
            }
        }
        
        return count > 0 ? total / count : 0;
    }
    
    /**
     * Combine spec and YouTube scores for a feature
     */
    private int combineScores(int specScore, int youtubeScore, Integer overallYoutubeScore) {
        // If no YouTube score available, use spec only
        if (youtubeScore == 0 && (overallYoutubeScore == null || overallYoutubeScore == 0)) {
            return specScore;
        }
        
        // Combine with weights
        double combined = specScore * 0.6 + youtubeScore * 0.4;
        return Math.max(0, Math.min(100, (int) Math.round(combined)));
    }
    
    /**
     * Result class for detailed scoring breakdown
     */
    public static class ScoringBreakdown {
        private final int unifiedScore;
        private final int specScore;
        private final Integer youtubeScore;
        private final Integer redditScore;
        private final boolean hasConsensusBonus;
        private final Map<String, Integer> featureScores;
        
        public ScoringBreakdown(int unifiedScore, int specScore, Integer youtubeScore, 
                               Integer redditScore, boolean hasConsensusBonus,
                               Map<String, Integer> featureScores) {
            this.unifiedScore = unifiedScore;
            this.specScore = specScore;
            this.youtubeScore = youtubeScore;
            this.redditScore = redditScore;
            this.hasConsensusBonus = hasConsensusBonus;
            this.featureScores = featureScores;
        }
        
        public int getUnifiedScore() { return unifiedScore; }
        public int getSpecScore() { return specScore; }
        public Integer getYoutubeScore() { return youtubeScore; }
        public Integer getRedditScore() { return redditScore; }
        public boolean hasConsensusBonus() { return hasConsensusBonus; }
        public Map<String, Integer> getFeatureScores() { return featureScores; }
    }
}
