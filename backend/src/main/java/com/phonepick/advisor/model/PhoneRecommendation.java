package com.phonepick.advisor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRecommendation {
    
    private Long id;
    private String brand;
    private String model;
    private Integer price;
    private Integer matchScore;
    private Integer youtubeSentimentScore; // Sentiment from YouTube analysis
    private Integer redditSentimentScore; // Sentiment from Reddit analysis
    
    // Specifications
    private Map<String, String> specs;
    
    // Feature scores
    private Map<String, Integer> scores;
    
    // Affiliate links
    private Map<String, String> affiliateLinks;
    
    // Image
    private String image;
    
    // Insights (simple conversational text)
    private String whyPicked;
    private String whyLoveIt;
    private String whatToKnow;
    
    // NEW: Dynamic comparisons with alternatives
    private List<AlternativeComparison> beatsAlternatives;
    
    // Inner class for alternative comparisons
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeComparison {
        private String brand;
        private String model;
        private String reason;
    }
    
    // Static factory method to create from Phone entity
    public static PhoneRecommendation fromPhone(Phone phone, int matchScore) {
        PhoneRecommendation recommendation = new PhoneRecommendation();
        recommendation.setId(phone.getId());
        recommendation.setBrand(phone.getBrand());
        recommendation.setModel(phone.getModel());
        recommendation.setPrice(phone.getPrice());
        recommendation.setMatchScore(matchScore);
        recommendation.setYoutubeSentimentScore(phone.getYoutubeSentimentScore());
        recommendation.setRedditSentimentScore(phone.getRedditSentimentScore());
        
        // Specs
        recommendation.setSpecs(Map.of(
            "display", phone.getDisplayInfo() != null ? phone.getDisplayInfo() : "N/A",
            "processor", phone.getProcessor() != null ? phone.getProcessor() : "N/A",
            "memory", phone.getMemory_and_storage() != null ? phone.getMemory_and_storage() : "N/A",
            "battery", phone.getBattery() != null ? phone.getBattery() : "N/A",
            "camera", phone.getCameraInfo() != null ? phone.getCameraInfo() : "N/A"
        ));
        
        // Scores
        recommendation.setScores(Map.of(
            "camera", phone.getCameraScore(),
            "battery", phone.getBatteryScore(),
            "software", phone.getSoftwareScore(),
            "privacy", phone.getPrivacyScore(),
            "looks", phone.getLooksScore()
        ));
        
        // Affiliate links
        recommendation.setAffiliateLinks(Map.of(
            "amazon", phone.getAffiliateAmazon() != null ? phone.getAffiliateAmazon() : "#",
            "flipkart", phone.getAffiliateFlipkart() != null ? phone.getAffiliateFlipkart() : "#"
        ));
        
        recommendation.setImage(phone.getImageUrl() != null ? phone.getImageUrl() : 
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=800&fit=crop");
        
        recommendation.setBeatsAlternatives(new ArrayList<>());
        
        return recommendation;
    }
    
    // Method to add insights
    public void addInsights(PhoneInsight insight) {
        if (insight != null) {
            this.whyPicked = insight.getWhyPicked();
            this.whyLoveIt = insight.getWhyLoveIt();
            this.whatToKnow = insight.getWhatToKnow();
        }
    }
    
    // Method to add alternative comparison
    public void addAlternativeComparison(String brand, String model, String reason) {
        if (beatsAlternatives == null) {
            beatsAlternatives = new ArrayList<>();
        }
        beatsAlternatives.add(new AlternativeComparison(brand, model, reason));
    }
}
