package com.phonepick.advisor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing YouTube channels for future automation
 * Tracks channel credibility and metadata
 */
@Entity
@Table(name = "youtube_channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeChannel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "channel_id", unique = true, nullable = false, length = 100)
    private String channelId;
    
    @Column(name = "channel_name", nullable = false)
    private String channelName;
    
    // Channel statistics
    @Column(name = "subscriber_count")
    private Long subscriberCount = 0L;
    
    @Column(name = "total_videos")
    private Integer totalVideos = 0;
    
    // Credibility scoring (0-100)
    @Column(name = "credibility_score", nullable = false)
    private Integer credibilityScore = 50;
    
    // Tier: 1 (best), 2, 3
    @Column(name = "tier")
    private Integer tier = 3;
    
    // Channel characteristics
    @Column(name = "language", length = 10)
    private String language = "EN";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "focus_areas", columnDefinition = "jsonb")
    private List<String> focusAreas;
    
    // Metadata
    @Column(name = "verified")
    private Boolean verified = false;
    
    @Column(name = "active")
    private Boolean active = true;
    
    // Timestamps
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
