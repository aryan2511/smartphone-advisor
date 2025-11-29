package com.phonepick.advisor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_id", nullable = false)
    private Phone phone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private YouTubeChannel channel;
    
    @Column(name = "video_id", nullable = false, length = 20)
    private String videoId;
    
    @Column(name = "video_title", length = 500)
    private String videoTitle;
    
    @Column(name = "video_url", nullable = false, length = 200)
    private String videoUrl;
    
    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;
    
    @Column(name = "sentiment_score")
    private Integer sentimentScore;
    
    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
