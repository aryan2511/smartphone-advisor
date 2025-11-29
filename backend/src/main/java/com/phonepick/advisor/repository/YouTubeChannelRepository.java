package com.phonepick.advisor.repository;

import com.phonepick.advisor.model.YouTubeChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing YouTube channels
 */
@Repository
public interface YouTubeChannelRepository extends JpaRepository<YouTubeChannel, Long> {
    
    /**
     * Find channel by YouTube channel ID
     */
    Optional<YouTubeChannel> findByChannelId(String channelId);
    
    /**
     * Find all active channels
     */
    List<YouTubeChannel> findByActiveTrue();
    
    /**
     * Find channels by tier
     */
    List<YouTubeChannel> findByTierOrderByCredibilityScoreDesc(Integer tier);
    
    /**
     * Find verified channels
     */
    List<YouTubeChannel> findByVerifiedTrueAndActiveTrue();
    
    /**
     * Find top credibility channels
     */
    List<YouTubeChannel> findTop10ByActiveTrueOrderByCredibilityScoreDesc();
}
