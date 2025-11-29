package com.phonepick.advisor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity representing user-facing insights
 * Contains simple, conversational text that users see
 */
@Entity
@Table(name = "phone_insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneInsight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone_id", nullable = false)
    private Long phoneId;
    
    /**
     * Priority pattern this insight applies to
     * Format: "camera-performance-battery-privacy-design"
     */
    @Column(name = "priority_pattern", length = 100)
    private String priorityPattern;
    
    /**
     * Simple conversational text explaining why we picked this phone
     * Example: "You wanted a phone that takes amazing photos first..."
     */
    @Column(name = "why_picked", columnDefinition = "TEXT")
    private String whyPicked;
    
    /**
     * Bullet points of why the user will love this phone
     * Example: "• Your Instagram will look professional\n• Night shots that actually look good"
     */
    @Column(name = "why_love_it", columnDefinition = "TEXT")
    private String whyLoveIt;
    
    /**
     * Honest trade-offs or things to be aware of
     * Example: "The battery is good but not the longest-lasting..."
     */
    @Column(name = "what_to_know", columnDefinition = "TEXT")
    private String whatToKnow;
    
    /**
     * ID of the alternative phone this beats
     */
    @Column(name = "beats_alternative_id")
    private Long beatsAlternativeId;
    
    /**
     * Reason why it beats the alternative
     * Example: "Better zoom capabilities and ₹15,000 cheaper"
     */
    @Column(name = "beats_reason", columnDefinition = "TEXT")
    private String beatsReason;
    
    /**
     * List of phones that weren't recommended and why
     * Format: [{"phone_id": 2, "reason": "camera not as sharp"}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rejected_phones", columnDefinition = "jsonb")
    private List<Map<String, Object>> rejectedPhones;
    
    /**
     * Whether this insight was manually written or AI-generated
     */
    @Column(name = "is_manual")
    private Boolean isManual = true;
    
    // Timestamps
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
