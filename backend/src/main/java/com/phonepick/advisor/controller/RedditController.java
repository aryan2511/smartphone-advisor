package com.phonepick.advisor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.phonepick.advisor.model.RedditPost;
import com.phonepick.advisor.service.RedditBatchService;
import com.phonepick.advisor.service.RedditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reddit")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RedditController {
    
    private static final Logger logger = LoggerFactory.getLogger(RedditController.class);
    
    private final RedditService redditService;
    private final RedditBatchService redditBatchService;
    
    /**
     * Manual trigger for Reddit post fetching
     */
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> triggerFetch() {
        logger.info("Manual Reddit fetch triggered via API");
        
        try {
            redditBatchService.triggerManualFetch();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reddit post fetch started successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering Reddit fetch: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Manual trigger for Reddit sentiment analysis
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> triggerAnalysis() {
        logger.info("Manual Reddit sentiment analysis triggered via API");
        
        try {
            redditBatchService.triggerManualAnalysis();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reddit sentiment analysis started successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering Reddit analysis: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get Reddit posts for a specific phone
     */
    @GetMapping("/phone/{phoneId}")
    public ResponseEntity<List<RedditPost>> getPostsForPhone(@PathVariable Long phoneId) {
        logger.info("Fetching Reddit posts for phone ID: {}", phoneId);
        
        try {
            List<RedditPost> posts = redditService.getPostsForPhone(phoneId);
            return ResponseEntity.ok(posts);
            
        } catch (Exception e) {
            logger.error("Error fetching Reddit posts: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get average sentiment score for a phone
     */
    @GetMapping("/phone/{phoneId}/sentiment")
    public ResponseEntity<Map<String, Object>> getSentimentScore(@PathVariable Long phoneId) {
        logger.info("Fetching Reddit sentiment score for phone ID: {}", phoneId);
        
        try {
            Double avgScore = redditService.getAverageSentimentScore(phoneId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("phoneId", phoneId);
            response.put("averageSentimentScore", avgScore);
            response.put("source", "reddit");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching sentiment score: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Search Reddit for a specific phone model
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchForPhone(@RequestParam String phoneModel) {
        logger.info("Searching Reddit for phone: {}", phoneModel);
        
        try {
            List<RedditPost> posts = redditService.searchRedditForPhone(phoneModel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("phoneModel", phoneModel);
            response.put("postsFound", posts.size());
            response.put("posts", posts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching Reddit: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}