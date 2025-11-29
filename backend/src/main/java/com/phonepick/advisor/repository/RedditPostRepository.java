package com.phonepick.advisor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.phonepick.advisor.model.RedditPost;

@Repository
public interface RedditPostRepository extends JpaRepository<RedditPost, Long> {
    
    Optional<RedditPost> findByPostId(String postId);
    
    List<RedditPost> findByPhoneId(Long phoneId);
    
    @Query("SELECT rp FROM RedditPost rp WHERE rp.phone.id = :phoneId AND rp.sentimentScore IS NOT NULL")
    List<RedditPost> findAnalyzedPostsByPhoneId(Long phoneId);
    
    @Query("SELECT rp FROM RedditPost rp WHERE rp.sentimentScore IS NULL")
    List<RedditPost> findPostsNeedingAnalysis();
    
    @Query("SELECT AVG(rp.sentimentScore) FROM RedditPost rp WHERE rp.phone.id = :phoneId AND rp.sentimentScore IS NOT NULL")
    Double getAverageSentimentScoreByPhoneId(Long phoneId);
}