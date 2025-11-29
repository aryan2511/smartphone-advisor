package com.phonepick.advisor.service;

import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.model.PhoneReview;
import com.phonepick.advisor.repository.PhoneRepository;
import com.phonepick.advisor.repository.PhoneReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Batch service to pre-fetch transcripts and analyze sentiment
 * Runs on a schedule to avoid YouTube API quota issues
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptBatchService {
    
    private final PhoneReviewRepository reviewRepository;
    private final PhoneRepository phoneRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${youtube.transcript.api.url:https://youtube-transcript-api.fly.dev/transcript}")
    private String transcriptApiUrl;
    
    @Value("${batch.processing.enabled:false}")
    private boolean batchProcessingEnabled;
    
    /**
     * Scheduled job to fetch missing transcripts
     * Runs daily at 2 AM to avoid peak hours
     * Processes 10 videos at a time to respect rate limits
     */
    @Scheduled(cron = "${batch.transcript.cron:0 0 2 * * ?}")
    @Transactional
    public void fetchMissingTranscripts() {
        if (!batchProcessingEnabled) {
            log.info("Batch processing is disabled");
            return;
        }
        
        log.info("Starting batch transcript fetch job");
        
        // Get reviews without transcripts
        List<PhoneReview> reviewsToProcess = reviewRepository.findByTranscriptIsNull();
        
        if (reviewsToProcess.isEmpty()) {
            log.info("No reviews need transcript fetching");
            return;
        }
        
        log.info("Found {} reviews to process", reviewsToProcess.size());
        
        int successCount = 0;
        int failCount = 0;
        int processLimit = 10; // Process only 10 per run to avoid overwhelming API
        
        for (int i = 0; i < Math.min(reviewsToProcess.size(), processLimit); i++) {
            PhoneReview review = reviewsToProcess.get(i);
            
            try {
                log.info("Fetching transcript for video: {}", review.getVideoId());
                
                String transcript = fetchTranscript(review.getVideoId());
                
                if (transcript != null && !transcript.isEmpty()) {
                    review.setTranscript(transcript);
                    reviewRepository.save(review);
                    successCount++;
                    log.info("Successfully fetched transcript for: {}", review.getVideoTitle());
                    
                    // Small delay to respect rate limits
                    Thread.sleep(2000);
                } else {
                    failCount++;
                    log.warn("Empty transcript for: {}", review.getVideoTitle());
                }
                
            } catch (Exception e) {
                failCount++;
                log.error("Failed to fetch transcript for {}: {}", review.getVideoId(), e.getMessage());
            }
        }
        
        log.info("Batch transcript fetch complete. Success: {}, Failed: {}", successCount, failCount);
    }
    
    /**
     * Scheduled job to analyze sentiment for transcripts
     * Runs daily at 3 AM (after transcript fetch)
     */
    @Scheduled(cron = "${batch.sentiment.cron:0 0 3 * * ?}")
    @Transactional
    public void analyzeMissingSentiments() {
        if (!batchProcessingEnabled) {
            return;
        }
        
        log.info("Starting batch sentiment analysis job");
        
        // Get reviews with transcripts but no sentiment score
        List<PhoneReview> reviewsToAnalyze = reviewRepository.findBySentimentScoreIsNull();
        
        if (reviewsToAnalyze.isEmpty()) {
            log.info("No reviews need sentiment analysis");
            return;
        }
        
        log.info("Found {} reviews to analyze", reviewsToAnalyze.size());
        
        int successCount = 0;
        
        for (PhoneReview review : reviewsToAnalyze) {
            if (review.getTranscript() == null || review.getTranscript().isEmpty()) {
                continue;
            }
            
            try {
                int sentimentScore = sentimentAnalysisService.analyzeSentiment(review.getTranscript());
                review.setSentimentScore(sentimentScore);
                review.setAnalyzedAt(LocalDateTime.now());
                reviewRepository.save(review);
                successCount++;
                
                log.info("Analyzed sentiment for: {} - Score: {}", review.getVideoTitle(), sentimentScore);
                
            } catch (Exception e) {
                log.error("Failed to analyze sentiment for {}: {}", review.getVideoId(), e.getMessage());
            }
        }
        
        log.info("Batch sentiment analysis complete. Analyzed: {} reviews", successCount);
        
        // Update phone aggregate scores
        updatePhoneAggregateScores();
    }
    
    /**
     * Update aggregate sentiment scores for all phones
     */
    @Transactional
    public void updatePhoneAggregateScores() {
        log.info("Updating aggregate sentiment scores for phones");
        
        List<Phone> phones = phoneRepository.findAll();
        int updatedCount = 0;
        
        for (Phone phone : phones) {
            List<PhoneReview> reviews = reviewRepository.findByPhoneIdAndTranscriptIsNotNull(phone.getId());
            
            if (reviews.isEmpty()) {
                continue;
            }
            
            // Calculate average sentiment from all reviews
            int totalScore = 0;
            int count = 0;
            
            for (PhoneReview review : reviews) {
                if (review.getSentimentScore() != null) {
                    totalScore += review.getSentimentScore();
                    count++;
                }
            }
            
            if (count > 0) {
                int avgScore = totalScore / count;
                phone.setYoutubeSentimentScore(avgScore);
                phoneRepository.save(phone);
                updatedCount++;
                
                log.info("Updated {} with avg sentiment: {} (from {} reviews)", 
                        phone.getModel(), avgScore, count);
            }
        }
        
        log.info("Updated {} phones with aggregate sentiment scores", updatedCount);
    }
    
    /**
     * Manual trigger to process all pending work
     * Can be called via API endpoint
     */
    @Transactional
    public String processAllPending() {
        fetchMissingTranscripts();
        analyzeMissingSentiments();
        return String.format("Batch processing complete");
    }
    
    /**
     * Fetch transcript from API
     */
    private String fetchTranscript(String videoId) {
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl(transcriptApiUrl)
                .queryParam("video_id", videoId)
                .build()
                .toUriString();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("transcript")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> transcript = 
                    (List<Map<String, String>>) response.get("transcript");
                
                StringBuilder fullText = new StringBuilder();
                for (Map<String, String> segment : transcript) {
                    fullText.append(segment.get("text")).append(" ");
                }
                
                return fullText.toString().trim();
            }
        } catch (Exception e) {
            log.error("Error fetching transcript: {}", e.getMessage());
        }
        
        return null;
    }
}
