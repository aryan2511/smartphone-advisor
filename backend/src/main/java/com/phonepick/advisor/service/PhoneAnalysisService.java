package com.phonepick.advisor.service;

import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.repository.PhoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced phone analysis service using feature-based sentiment analysis
 * Integrates YouTubeService and FeatureSentimentAnalysisService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneAnalysisService {
    
    private final YouTubeService youtubeService;
    private final FeatureSentimentAnalysisService sentimentAnalysisService;
    private final UnifiedScoringService unifiedScoringService;
    private final PhoneRepository phoneRepository;
    
    /**
     * Analyze a phone by checking YouTube videos and running feature-based sentiment analysis
     * Updates the phone's sentiment score in the database
     * 
     * @param phoneId The phone ID to analyze
     * @return Analysis result containing sentiment scores
     */
    @Transactional
    public PhoneAnalysisResult analyzePhone(Long phoneId) {
        Phone phone = phoneRepository.findById(phoneId)
            .orElseThrow(() -> new IllegalArgumentException("Phone not found: " + phoneId));
        
        String phoneModel = phone.getBrand() + " " + phone.getModel();
        log.info("Starting feature-based analysis for: {}", phoneModel);
        
        // Step 1: Find videos for this phone
        Map<String, YouTubeService.VideoInfo> videos = youtubeService.findVideosForPhone(phoneModel);
        
        if (videos.isEmpty()) {
            log.warn("No videos found for: {}", phoneModel);
            return new PhoneAnalysisResult(
                phoneId, phoneModel, 50, new HashMap<>(), new HashMap<>(),
                "No YouTube videos found for analysis", false
            );
        }
        
        // Step 2: Fetch transcripts and analyze feature-based sentiment
        Map<String, Integer> channelOverallScores = new HashMap<>();
        Map<String, Map<String, Integer>> channelFeatureScores = new HashMap<>();
        
        for (Map.Entry<String, YouTubeService.VideoInfo> entry : videos.entrySet()) {
            String channelName = entry.getKey();
            YouTubeService.VideoInfo videoInfo = entry.getValue();
            
            log.info("Fetching transcript from {}: {}", channelName, videoInfo.getTitle());
            
            String transcript = youtubeService.fetchTranscript(videoInfo.getVideoId());
            
            if (transcript != null && !transcript.isEmpty()) {
                // Analyze feature sentiments
                Map<String, Integer> featureScores = sentimentAnalysisService.analyzeFeatureSentiments(transcript);
                
                // Calculate overall score from features
                int overallScore = sentimentAnalysisService.calculateOverallSentiment(featureScores);
                
                channelOverallScores.put(channelName, overallScore);
                channelFeatureScores.put(channelName, featureScores);
                
                log.info("Feature scores from {}: camera={}, battery={}, performance={}, overall={}",
                        channelName,
                        featureScores.getOrDefault("camera", 0),
                        featureScores.getOrDefault("battery", 0),
                        featureScores.getOrDefault("performance", 0),
                        overallScore);
            } else {
                log.warn("Could not fetch transcript from {}", channelName);
            }
        }
        
        if (channelOverallScores.isEmpty()) {
            log.warn("No transcripts analyzed for: {}", phoneModel);
            return new PhoneAnalysisResult(
                phoneId, phoneModel, 50, new HashMap<>(), new HashMap<>(),
                "Transcripts could not be fetched", false
            );
        }
        
        // Step 3: Calculate average sentiment score
        int totalScore = channelOverallScores.values().stream().mapToInt(Integer::intValue).sum();
        int avgSentimentScore = totalScore / channelOverallScores.size();
        
        // Step 4: Check for consensus bonus
        boolean hasConsensus = unifiedScoringService.calculateUnifiedScore(phone, channelOverallScores) 
                              > avgSentimentScore;
        
        // Step 5: Update phone with YouTube sentiment score
        phone.setYoutubeSentimentScore(avgSentimentScore);
        phoneRepository.save(phone);
        
        log.info("Analysis complete for {}. Average sentiment: {}, Channels: {}, Consensus: {}",
                phoneModel, avgSentimentScore, channelOverallScores.size(), hasConsensus);
        
        return new PhoneAnalysisResult(
            phoneId,
            phoneModel,
            avgSentimentScore,
            channelOverallScores,
            channelFeatureScores,
            String.format("Analyzed %d video(s) with feature-based sentiment", channelOverallScores.size()),
            hasConsensus
        );
    }
    
    /**
     * Batch analyze multiple phones
     */
    public Map<Long, PhoneAnalysisResult> analyzePhones(Iterable<Long> phoneIds) {
        Map<Long, PhoneAnalysisResult> results = new HashMap<>();
        
        for (Long phoneId : phoneIds) {
            try {
                PhoneAnalysisResult result = analyzePhone(phoneId);
                results.put(phoneId, result);
                
                // Add delay to avoid API rate limits
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Batch analysis interrupted for phone {}", phoneId);
                break;
            } catch (Exception e) {
                log.error("Error analyzing phone {}: {}", phoneId, e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Enhanced analysis result with feature scores
     */
    public static class PhoneAnalysisResult {
        private final Long phoneId;
        private final String phoneModel;
        private final int averageSentimentScore;
        private final Map<String, Integer> channelScores;
        private final Map<String, Map<String, Integer>> channelFeatureScores;
        private final String message;
        private final boolean hasConsensus;
        
        public PhoneAnalysisResult(Long phoneId, String phoneModel, int averageSentimentScore,
                                  Map<String, Integer> channelScores,
                                  Map<String, Map<String, Integer>> channelFeatureScores,
                                  String message, boolean hasConsensus) {
            this.phoneId = phoneId;
            this.phoneModel = phoneModel;
            this.averageSentimentScore = averageSentimentScore;
            this.channelScores = channelScores;
            this.channelFeatureScores = channelFeatureScores;
            this.message = message;
            this.hasConsensus = hasConsensus;
        }
        
        public Long getPhoneId() { return phoneId; }
        public String getPhoneModel() { return phoneModel; }
        public int getAverageSentimentScore() { return averageSentimentScore; }
        public Map<String, Integer> getChannelScores() { return channelScores; }
        public Map<String, Map<String, Integer>> getChannelFeatureScores() { return channelFeatureScores; }
        public String getMessage() { return message; }
        public boolean hasConsensus() { return hasConsensus; }
        
        /**
         * Get average feature scores across all channels
         */
        public Map<String, Integer> getAverageFeatureScores() {
            Map<String, Integer> avgScores = new HashMap<>();
            Map<String, Integer> counts = new HashMap<>();
            
            for (Map<String, Integer> channelFeatures : channelFeatureScores.values()) {
                for (Map.Entry<String, Integer> entry : channelFeatures.entrySet()) {
                    String feature = entry.getKey();
                    int score = entry.getValue();
                    
                    avgScores.put(feature, avgScores.getOrDefault(feature, 0) + score);
                    counts.put(feature, counts.getOrDefault(feature, 0) + 1);
                }
            }
            
            // Calculate averages
            for (String feature : avgScores.keySet()) {
                avgScores.put(feature, avgScores.get(feature) / counts.get(feature));
            }
            
            return avgScores;
        }
    }
}
