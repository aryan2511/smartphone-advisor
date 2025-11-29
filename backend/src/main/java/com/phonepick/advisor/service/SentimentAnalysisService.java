package com.phonepick.advisor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for analyzing sentiment from video transcripts
 */
@Service
@Slf4j
public class SentimentAnalysisService {
    
    private static final Set<String> POSITIVE_WORDS = Set.of(
        "excellent", "amazing", "great", "good", "best", "fantastic", "outstanding",
        "impressive", "solid", "reliable", "recommend", "love", "perfect", "brilliant",
        "exceptional", "superb", "wonderful", "quality", "premium", "top", "superior",
        "awesome", "powerful", "smooth", "fast", "long", "clear", "bright", "sharp","night photo","depth of field"
    );
    
    private static final Set<String> NEGATIVE_WORDS = Set.of(
        "bad", "poor", "worst", "terrible", "awful", "disappointing", "mediocre",
        "issue", "problem", "flaw", "weak", "slow", "lag", "buggy", "worse",
        "overpriced", "expensive", "cheap", "plasticky", "fragile", "heating",
        "battery drain", "camera issue", "not good", "don't recommend", "avoid"
    );
    
    private static final Map<String, String> FEATURE_KEYWORDS = Map.of(
        "camera", "camera|photo|picture|video recording|portrait|night mode|lens",
        "battery", "battery|charge|charging|power|mah|backup",
        "performance", "performance|speed|processor|gaming|multitask|ram|lag",
        "display", "display|screen|brightness|refresh rate|amoled|lcd",
        "design", "design|build|premium|glass|metal|plastic|weight|feel"
    );
    
    /**
     * Analyze transcript and return sentiment score (0-100)
     * @param transcript The video transcript text
     * @return Sentiment score (0-100) where higher is more positive
     */
    public int analyzeSentiment(String transcript) {
        if (transcript == null || transcript.isEmpty()) {
            log.warn("Empty transcript provided for sentiment analysis");
            return 50; // Neutral score
        }
        
        String lowerTranscript = transcript.toLowerCase();
        
        // Count positive and negative words
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : POSITIVE_WORDS) {
            positiveCount += countOccurrences(lowerTranscript, word);
        }
        
        for (String word : NEGATIVE_WORDS) {
            negativeCount += countOccurrences(lowerTranscript, word);
        }
        
        // Calculate sentiment score
        int totalSentimentWords = positiveCount + negativeCount;
        if (totalSentimentWords == 0) {
            return 50; // Neutral if no sentiment words found
        }
        
        // Score = 50 + (positive ratio - 0.5) * 100
        double positiveRatio = (double) positiveCount / totalSentimentWords;
        int score = (int) Math.round(50 + (positiveRatio - 0.5) * 100);
        
        // Clamp to 0-100
        score = Math.max(0, Math.min(100, score));
        
        log.info("Sentiment analysis: {} positive, {} negative, score: {}", 
                 positiveCount, negativeCount, score);
        
        return score;
    }
    
    /**
     * Extract feature-specific sentiments from transcript
     * @param transcript The video transcript text
     * @return Map of feature to sentiment score
     */
    public Map<String, Integer> analyzeFeatureSentiments(String transcript) {
        Map<String, Integer> featureScores = new HashMap<>();
        
        if (transcript == null || transcript.isEmpty()) {
            return featureScores;
        }
        
        String lowerTranscript = transcript.toLowerCase();
        
        for (Map.Entry<String, String> entry : FEATURE_KEYWORDS.entrySet()) {
            String feature = entry.getKey();
            String pattern = entry.getValue();
            
            // Extract sentences mentioning this feature
            List<String> featureSentences = extractFeatureSentences(lowerTranscript, pattern);
            
            if (!featureSentences.isEmpty()) {
                int featureScore = analyzeSentiment(String.join(" ", featureSentences));
                featureScores.put(feature, featureScore);
            }
        }
        
        return featureScores;
    }
    
    /**
     * Extract sentences containing feature keywords
     */
    private List<String> extractFeatureSentences(String text, String keywordPattern) {
        List<String> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile("([^.!?]*\\b(" + keywordPattern + ")\\b[^.!?]*[.!?])", 
                                         Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            sentences.add(matcher.group(1).trim());
        }
        
        return sentences;
    }
    
    /**
     * Count occurrences of a word in text
     */
    private int countOccurrences(String text, String word) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    /**
     * Generate a summary of the sentiment analysis
     */
    public SentimentSummary generateSummary(String transcript) {
        int overallScore = analyzeSentiment(transcript);
        Map<String, Integer> featureScores = analyzeFeatureSentiments(transcript);
        
        return new SentimentSummary(overallScore, featureScores);
    }
    
    /**
     * Data class for sentiment summary
     */
    public static class SentimentSummary {
        private final int overallScore;
        private final Map<String, Integer> featureScores;
        
        public SentimentSummary(int overallScore, Map<String, Integer> featureScores) {
            this.overallScore = overallScore;
            this.featureScores = featureScores;
        }
        
        public int getOverallScore() { return overallScore; }
        public Map<String, Integer> getFeatureScores() { return featureScores; }
    }
}
