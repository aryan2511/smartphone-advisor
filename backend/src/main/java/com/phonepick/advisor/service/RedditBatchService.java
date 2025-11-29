package com.phonepick.advisor.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.phonepick.advisor.model.RedditPost;
import com.phonepick.advisor.repository.RedditPostRepository;

@Service
public class RedditBatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedditBatchService.class);
    
    private final RedditService redditService;
    private final RedditPostRepository redditPostRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final com.phonepick.advisor.repository.PhoneRepository phoneRepository;
    
    @Value("${batch.processing.enabled:true}")
    private boolean batchProcessingEnabled;
    
    public RedditBatchService(
            RedditService redditService,
            RedditPostRepository redditPostRepository,
            SentimentAnalysisService sentimentAnalysisService,
            com.phonepick.advisor.repository.PhoneRepository phoneRepository) {
        this.redditService = redditService;
        this.redditPostRepository = redditPostRepository;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.phoneRepository = phoneRepository;
    }
    
    /**
     * Scheduled task to fetch Reddit posts daily at 4 AM
     */
    @Scheduled(cron = "${batch.reddit.fetch.cron:0 0 4 * * ?}")
    public void scheduledRedditFetch() {
        if (!batchProcessingEnabled) {
            logger.info("Batch processing is disabled. Skipping scheduled Reddit fetch.");
            return;
        }
        
        logger.info("Starting scheduled Reddit post fetch at {}", LocalDateTime.now());
        
        try {
            redditService.batchFetchRedditPosts();
            logger.info("Scheduled Reddit fetch completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled Reddit fetch: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Scheduled task to analyze Reddit posts daily at 5 AM
     */
    @Scheduled(cron = "${batch.reddit.analysis.cron:0 0 5 * * ?}")
    public void scheduledRedditSentimentAnalysis() {
        if (!batchProcessingEnabled) {
            logger.info("Batch processing is disabled. Skipping scheduled Reddit sentiment analysis.");
            return;
        }
        
        logger.info("Starting scheduled Reddit sentiment analysis at {}", LocalDateTime.now());
        
        try {
            List<RedditPost> postsToAnalyze = redditPostRepository.findPostsNeedingAnalysis();
            logger.info("Found {} Reddit posts needing sentiment analysis", postsToAnalyze.size());
            
            int analyzed = 0;
            for (RedditPost post : postsToAnalyze) {
                try {
                    analyzeRedditPost(post);
                    analyzed++;
                    
                    // Small delay to avoid overwhelming the sentiment service
                    if (analyzed % 10 == 0) {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    logger.error("Error analyzing Reddit post {}: {}", post.getPostId(), e.getMessage());
                }
            }
            
            logger.info("Scheduled Reddit sentiment analysis completed. Analyzed {} posts", analyzed);
            
            // Update aggregate Reddit sentiment scores for phones
            updatePhoneRedditScores();
        } catch (Exception e) {
            logger.error("Error during scheduled Reddit sentiment analysis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Analyze sentiment of a single Reddit post
     */
    private void analyzeRedditPost(RedditPost post) {
        try {
            String textToAnalyze = combinePostContent(post);
            
            if (textToAnalyze.isEmpty()) {
                logger.warn("No content to analyze for Reddit post {}", post.getPostId());
                return;
            }
            
            int sentimentScore = sentimentAnalysisService.analyzeSentiment(textToAnalyze);
            
            post.setSentimentScore(sentimentScore);
            post.setAnalyzedAt(LocalDateTime.now());
            
            redditPostRepository.save(post);
            
            logger.debug("Analyzed Reddit post {}: sentiment score = {}", post.getPostId(), sentimentScore);
            
        } catch (Exception e) {
            logger.error("Error analyzing sentiment for post {}: {}", post.getPostId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Combine post title and content for sentiment analysis
     */
    private String combinePostContent(RedditPost post) {
        StringBuilder combined = new StringBuilder();
        
        if (post.getPostTitle() != null && !post.getPostTitle().isEmpty()) {
            combined.append(post.getPostTitle()).append(". ");
        }
        
        if (post.getContent() != null && !post.getContent().isEmpty()) {
            combined.append(post.getContent());
        }
        
        return combined.toString().trim();
    }
    
    /**
     * Manual trigger for Reddit fetch (for testing)
     */
    public void triggerManualFetch() {
        logger.info("Manual Reddit fetch triggered");
        redditService.batchFetchRedditPosts();
    }
    
    /**
     * Manual trigger for sentiment analysis (for testing)
     */
    public void triggerManualAnalysis() {
        logger.info("Manual Reddit sentiment analysis triggered");
        scheduledRedditSentimentAnalysis();
    }
    
    /**
     * Update aggregate Reddit sentiment scores for all phones
     */
    private void updatePhoneRedditScores() {
        logger.info("Updating aggregate Reddit sentiment scores for phones");
        
        List<com.phonepick.advisor.model.Phone> phones = phoneRepository.findAll();
        int updatedCount = 0;
        
        for (com.phonepick.advisor.model.Phone phone : phones) {
            try {
                Double avgScore = redditService.getAverageSentimentScore(phone.getId());
                
                if (avgScore != null) {
                    phone.setRedditSentimentScore(avgScore.intValue());
                    phoneRepository.save(phone);
                    updatedCount++;
                    
                    logger.debug("Updated {} with Reddit sentiment: {}", 
                            phone.getModel(), avgScore.intValue());
                }
            } catch (Exception e) {
                logger.error("Error updating Reddit score for {}: {}", 
                        phone.getModel(), e.getMessage());
            }
        }
        
        logger.info("Updated {} phones with Reddit sentiment scores", updatedCount);
    }
}