package com.phonepick.advisor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "phones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Phone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String brand;
    
    @Column(nullable = false)
    private String model;
    
    @Column(nullable = false)
    private Integer price;
    
    // Specifications from CSV
    private String memory_and_storage;
    
    @Column(name = "display_info")
    private String displayInfo;
    
    @Column(name = "camera")
    private String cameraInfo;

    private String processor;

    private String battery;

    // Image URL
    @Column(name = "image_url")
    private String imageUrl;
    
    // Feature scores (0-100) - calculated based on specs
    @Column(name = "camera_score")
    private Integer cameraScore;
    
    @Column(name = "battery_score")
    private Integer batteryScore;
    
    @Column(name = "software_score")
    private Integer softwareScore;
    
    @Column(name = "privacy_score")
    private Integer privacyScore;
    
    @Column(name = "looks_score")
    private Integer looksScore;
    
    // Legacy fields (for backward compatibility)
    // private String display;
    // private String camera;
    
    

    // Affiliate links
    @Column(name = "affiliate_amazon")
    private String affiliateAmazon;
    
    @Column(name = "affiliate_flipkart")
    private String affiliateFlipkart;
    // YouTube sentiment score (0-100)
    @Column(name = "youtube_sentiment_score")
    private Integer youtubeSentimentScore;
    
    // Reddit sentiment score (0-100)
    @Column(name = "reddit_sentiment_score")
    private Integer redditSentimentScore;
    
    // Helper method for compatibility (returns the model field)
    public String getModelName() {
        return this.model;
    }
}
