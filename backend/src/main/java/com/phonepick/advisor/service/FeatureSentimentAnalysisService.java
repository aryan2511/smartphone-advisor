package com.phonepick.advisor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature-based sentiment analysis service
 * Analyzes transcripts based on specific phone features rather than generic positive/negative
 */
@Service
@Slf4j
public class FeatureSentimentAnalysisService {
    
    // Feature-specific keywords and sentiment words
    private static final Map<String, FeatureAnalysis> FEATURE_PATTERNS = Map.of(
        "camera", new FeatureAnalysis(
            "camera|photo|picture|video recording|portrait|night mode|lens|zoom|megapixel|mp camera|image quality|low light|selfie",
            Set.of("excellent photo", "amazing camera", "great photos", "sharp images", "clear pictures", 
                   "night mode", "good zoom", "detailed shots", "vibrant colors", "accurate colors",
                   "fast focus", "good portrait", "excellent video", "stable video", "4k video",
                   "optical zoom", "wide angle", "macro shots", "crisp", "clarity"),
            Set.of("poor camera", "bad photos", "blurry", "grainy", "noise", "washed out", 
                   "soft focus", "slow shutter", "bad low light", "overexposed", "underexposed",
                   "camera issue", "photo quality", "disappointing camera", "mediocre camera")
        ),
        
        "battery", new FeatureAnalysis(
            "battery|charge|charging|power|mah|backup|battery life|screen on time|sot|all day battery",
            Set.of("long battery", "excellent battery", "all day battery", "great battery life", 
                   "fast charging", "quick charge", "good backup", "lasts all day", "impressive battery",
                   "battery champ", "hours of use", "full day", "two days", "wireless charging",
                   "reverse charging", "battery saver", "efficient"),
            Set.of("poor battery", "bad battery", "battery drain", "heating", "overheating",
                   "short battery", "dies quickly", "needs frequent charging", "battery issue",
                   "disappointing battery", "average battery", "not enough battery")
        ),
        
        "performance", new FeatureAnalysis(
            "performance|speed|processor|gaming|multitask|ram|lag|chipset|snapdragon|mediatek|exynos|smooth|responsive|fps",
            Set.of("fast", "smooth", "powerful", "no lag", "snappy", "responsive", "excellent performance",
                   "handles multitasking", "gaming beast", "high fps", "good processor", "flagship performance",
                   "quick", "seamless", "fluid", "benchmark", "handles everything", "zero lag"),
            Set.of("slow", "lag", "laggy", "stuttering", "frame drops", "heating", "thermal throttling",
                   "poor performance", "struggles with", "app crashes", "freezes", "sluggish",
                   "disappointing performance", "not smooth", "choppy")
        ),
        
        "display", new FeatureAnalysis(
            "display|screen|brightness|refresh rate|amoled|oled|lcd|panel|viewing angles|sunlight|outdoor visibility|colors",
            Set.of("bright", "vibrant display", "excellent screen", "good brightness", "smooth display",
                   "120hz", "90hz", "high refresh", "amoled", "oled", "punchy colors", "great viewing",
                   "sharp screen", "beautiful display", "immersive", "excellent panel", "good colors"),
            Set.of("dim", "poor brightness", "washed out", "dull colors", "low brightness", "pwm flickering",
                   "bad viewing angles", "screen issue", "touch response", "ghost touch", "display problem",
                   "disappointing screen", "average display", "not bright enough")
        ),
        
        "design", new FeatureAnalysis(
            "design|build|premium|glass|metal|plastic|weight|feel|in hand|aesthetics|look|style|finish|compact|slim",
            Set.of("premium", "solid build", "great design", "beautiful", "premium feel", "well built",
                   "glass back", "metal frame", "good in hand", "lightweight", "comfortable", "sleek",
                   "elegant", "modern design", "compact", "good grip", "quality materials", "flagship feel"),
            Set.of("cheap", "plasticky", "fragile", "heavy", "bulky", "poor build", "creaky", "feels cheap",
                   "bad design", "fingerprint magnet", "slippery", "weak build", "disappointing design",
                   "average build", "not premium")
        )
    );
    
    /**
     * Analyze feature-specific sentiments from transcript
     * @return Map of feature to sentiment score (0-100)
     */
    public Map<String, Integer> analyzeFeatureSentiments(String transcript) {
        Map<String, Integer> featureScores = new HashMap<>();
        
        if (transcript == null || transcript.isEmpty()) {
            log.warn("Empty transcript provided");
            return featureScores;
        }
        
        String lowerTranscript = transcript.toLowerCase();
        
        for (Map.Entry<String, FeatureAnalysis> entry : FEATURE_PATTERNS.entrySet()) {
            String feature = entry.getKey();
            FeatureAnalysis analysis = entry.getValue();
            
            // Extract sentences mentioning this feature
            List<String> featureSentences = extractFeatureSentences(lowerTranscript, analysis.keywords);
            
            if (!featureSentences.isEmpty()) {
                int score = calculateFeatureScore(featureSentences, analysis);
                featureScores.put(feature, score);
                log.debug("Feature '{}': score={}, sentences analyzed={}", 
                         feature, score, featureSentences.size());
            }
        }
        
        return featureScores;
    }
    
    /**
     * Calculate sentiment score for a specific feature
     */
    private int calculateFeatureScore(List<String> sentences, FeatureAnalysis analysis) {
        String combinedText = String.join(" ", sentences).toLowerCase();
        
        int positiveCount = 0;
        int negativeCount = 0;
        
        // Count positive mentions
        for (String positive : analysis.positiveWords) {
            positiveCount += countOccurrences(combinedText, positive);
        }
        
        // Count negative mentions
        for (String negative : analysis.negativeWords) {
            negativeCount += countOccurrences(combinedText, negative);
        }
        
        int totalMentions = positiveCount + negativeCount;
        
        if (totalMentions == 0) {
            return 50; // Neutral if feature mentioned but no clear sentiment
        }
        
        // Calculate score: 50 + (positive ratio - 0.5) * 100
        double positiveRatio = (double) positiveCount / totalMentions;
        int score = (int) Math.round(50 + (positiveRatio - 0.5) * 100);
        
        // Clamp to 0-100
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Extract sentences containing feature keywords
     */
    private List<String> extractFeatureSentences(String text, String keywordPattern) {
        List<String> sentences = new ArrayList<>();
        
        // Split text into sentences
        String[] allSentences = text.split("[.!?]+");
        
        Pattern pattern = Pattern.compile("\\b(" + keywordPattern + ")\\b", Pattern.CASE_INSENSITIVE);
        
        for (String sentence : allSentences) {
            Matcher matcher = pattern.matcher(sentence);
            if (matcher.find()) {
                sentences.add(sentence.trim());
            }
        }
        
        return sentences;
    }
    
    /**
     * Count occurrences of a phrase in text
     */
    private int countOccurrences(String text, String phrase) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(phrase, index)) != -1) {
            count++;
            index += phrase.length();
        }
        
        return count;
    }
    
    /**
     * Get overall sentiment based on feature scores
     */
    public int calculateOverallSentiment(Map<String, Integer> featureScores) {
        if (featureScores.isEmpty()) {
            return 50;
        }
        
        int total = 0;
        for (int score : featureScores.values()) {
            total += score;
        }
        
        return total / featureScores.size();
    }
    
    /**
     * Data class for feature analysis configuration
     */
    private static class FeatureAnalysis {
        final String keywords;
        final Set<String> positiveWords;
        final Set<String> negativeWords;
        
        FeatureAnalysis(String keywords, Set<String> positiveWords, Set<String> negativeWords) {
            this.keywords = keywords;
            this.positiveWords = positiveWords;
            this.negativeWords = negativeWords;
        }
    }
    
    /**
     * Get detailed analysis result
     */
    public FeatureAnalysisResult analyzeWithDetails(String transcript) {
        Map<String, Integer> featureScores = analyzeFeatureSentiments(transcript);
        int overallScore = calculateOverallSentiment(featureScores);
        
        return new FeatureAnalysisResult(overallScore, featureScores);
    }
    
    /**
     * Result class
     */
    public static class FeatureAnalysisResult {
        private final int overallScore;
        private final Map<String, Integer> featureScores;
        
        public FeatureAnalysisResult(int overallScore, Map<String, Integer> featureScores) {
            this.overallScore = overallScore;
            this.featureScores = featureScores;
        }
        
        public int getOverallScore() { return overallScore; }
        public Map<String, Integer> getFeatureScores() { return featureScores; }
    }
}
