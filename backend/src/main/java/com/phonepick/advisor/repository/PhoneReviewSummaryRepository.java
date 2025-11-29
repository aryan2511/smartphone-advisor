package com.phonepick.advisor.repository;

import com.phonepick.advisor.model.PhoneReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing aggregated phone review summaries
 */
@Repository
public interface PhoneReviewSummaryRepository extends JpaRepository<PhoneReviewSummary, Long> {
    
    /**
     * Find summary by phone ID
     */
    Optional<PhoneReviewSummary> findByPhoneId(Long phoneId);
    
    /**
     * Check if summary exists for a phone
     */
    boolean existsByPhoneId(Long phoneId);
    
    /**
     * Delete summary by phone ID
     */
    void deleteByPhoneId(Long phoneId);
}
