package com.phonepick.advisor.repository;

import com.phonepick.advisor.model.PhoneInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing phone insights
 */
@Repository
public interface PhoneInsightRepository extends JpaRepository<PhoneInsight, Long> {
    
    /**
     * Find all insights for a specific phone
     */
    List<PhoneInsight> findByPhoneId(Long phoneId);
    
    /**
     * Find insight by phone ID and priority pattern
     */
    Optional<PhoneInsight> findByPhoneIdAndPriorityPattern(Long phoneId, String priorityPattern);
    
    /**
     * Find insights by priority pattern
     */
    List<PhoneInsight> findByPriorityPattern(String priorityPattern);
    
    /**
     * Find manually created insights
     */
    List<PhoneInsight> findByIsManualTrue();
    
    /**
     * Check if insight exists for phone and priority pattern
     */
    boolean existsByPhoneIdAndPriorityPattern(Long phoneId, String priorityPattern);
    
    /**
     * Find the first insight for a phone (fallback when no pattern match)
     */
    Optional<PhoneInsight> findFirstByPhoneId(Long phoneId);
    
    /**
     * Find the most relevant insight for a phone based on priority pattern
     * If exact match not found, returns any insight for that phone
     */
    @Query("SELECT pi FROM PhoneInsight pi WHERE pi.phoneId = :phoneId " +
           "ORDER BY CASE WHEN pi.priorityPattern = :priorityPattern THEN 0 ELSE 1 END, pi.id ASC")
    Optional<PhoneInsight> findBestMatchingInsight(@Param("phoneId") Long phoneId, 
                                                    @Param("priorityPattern") String priorityPattern);
}
