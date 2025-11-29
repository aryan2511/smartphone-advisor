package com.phonepick.advisor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing aggregated review summary for a phone
 * Combines data from multiple review sources
 */
@Entity
@Table(name = "phone_review_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneReviewSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone_id", unique = true, nullable = false)
    private Long phoneId;
    
    // Review counts by source
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Column(name = "reddit_count")
    private Integer redditCount = 0;
    
    @Column(name = "youtube_count")
    private Integer youtubeCount = 0;
    
    @Column(name = "amazon_count")
    private Integer amazonCount = 0;
    
    @Column(name = "flipkart_count")
    private Integer flipkartCount = 0;
    
    // Overall aggregated sentiment
    @Column(name = "overall_sentiment", precision = 5, scale = 2)
    private BigDecimal overallSentiment;
    
    // Feature-specific aggregated scores
    @Column(name = "camera_score", precision = 5, scale = 2)
    private BigDecimal cameraScore;
    
    @Column(name = "battery_score", precision = 5, scale = 2)
    private BigDecimal batteryScore;
    
    @Column(name = "performance_score", precision = 5, scale = 2)
    private BigDecimal performanceScore;
    
    @Column(name = "privacy_score", precision = 5, scale = 2)
    private BigDecimal privacyScore;
    
    @Column(name = "design_score", precision = 5, scale = 2)
    private BigDecimal designScore;
    
    // Common themes (stored as JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "common_pros", columnDefinition = "jsonb")
    private List<String> commonPros;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "common_cons", columnDefinition = "jsonb")
    private List<String> commonCons;
    
    // Data quality indicator
    @Column(name = "confidence_level", length = 20)
    private String confidenceLevel;
    
    // Timestamps
    @Column(name = "last_synced")
    private LocalDateTime lastSynced;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
