package com.phonepick.advisor.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reddit_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedditPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_id", nullable = false)
    private Phone phone;
    
    @Column(name = "post_id", nullable = false, unique = true, length = 20)
    private String postId;
    
    @Column(name = "post_title", length = 500)
    private String postTitle;
    
    @Column(name = "post_url", nullable = false, length = 500)
    private String postUrl;
    
    @Column(name = "author", length = 100)
    private String author;
    
    @Column(name = "subreddit", nullable = false, length = 100)
    private String subreddit;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "upvotes")
    private Integer upvotes;
    
    @Column(name = "num_comments")
    private Integer numComments;
    
    @Column(name = "sentiment_score")
    private Integer sentimentScore;
    
    @Column(name = "post_created_at")
    private LocalDateTime postCreatedAt;
    
    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}