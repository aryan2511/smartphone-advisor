package com.phonepick.advisor.repository;

import com.phonepick.advisor.model.PhoneReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneReviewRepository extends JpaRepository<PhoneReview, Long> {
    
    List<PhoneReview> findByPhoneId(Long phoneId);
    
    List<PhoneReview> findByPhoneIdAndTranscriptIsNotNull(Long phoneId);
    
    List<PhoneReview> findByTranscriptIsNull();
    
    List<PhoneReview> findBySentimentScoreIsNull();
}
