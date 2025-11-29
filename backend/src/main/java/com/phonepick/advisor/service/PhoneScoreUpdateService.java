package com.phonepick.advisor.service;

import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.repository.PhoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to update phone scores using enhanced scoring algorithms
 * Run this to migrate existing phones to new scoring system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneScoreUpdateService {
    
    private final PhoneRepository phoneRepository;
    private final PhoneSpecScoringService specScoringService;
    
    /**
     * Update all phones with new spec-based scores
     * This recalculates camera, battery, software, looks, and privacy scores
     */
    @Transactional
    public UpdateResult updateAllPhoneScores() {
        log.info("Starting phone score update with enhanced scoring...");
        
        List<Phone> allPhones = phoneRepository.findAll();
        int updated = 0;
        int failed = 0;
        
        for (Phone phone : allPhones) {
            try {
                updatePhoneScores(phone);
                phoneRepository.save(phone);
                updated++;
                
                if (updated % 10 == 0) {
                    log.info("Updated {} phones...", updated);
                }
            } catch (Exception e) {
                log.error("Failed to update phone {}: {}", phone.getId(), e.getMessage());
                failed++;
            }
        }
        
        log.info("Phone score update complete. Updated: {}, Failed: {}", updated, failed);
        return new UpdateResult(updated, failed);
    }
    
    /**
     * Update scores for a single phone
     */
    @Transactional
    public Phone updatePhoneScores(Phone phone) {
        log.debug("Updating scores for: {} {}", phone.getBrand(), phone.getModel());
        
        // Camera score
        int cameraScore = specScoringService.scoreCameraSpec(phone.getCameraInfo());
        phone.setCameraScore(cameraScore);
        
        // Battery score
        int batteryScore = specScoringService.scoreBatterySpec(phone.getBattery());
        phone.setBatteryScore(batteryScore);
        
        // Software/Performance score
        int perfScore = specScoringService.scoreProcessor(phone.getProcessor());
        phone.setSoftwareScore(perfScore);
        
        // Looks/Design score (based on display quality and build)
        int displayScore = specScoringService.scoreDisplay(phone.getDisplayInfo());
        int storageScore = specScoringService.scoreStorageAndRam(phone.getMemory_and_storage());
        
        // Looks is average of display and general build quality indicators
        int looksScore = (displayScore + getBuildQualityScore(phone)) / 2;
        phone.setLooksScore(looksScore);
        
        // Privacy score (keep existing or set based on brand reputation)
        if (phone.getPrivacyScore() == null || phone.getPrivacyScore() == 0) {
            phone.setPrivacyScore(getPrivacyScore(phone.getBrand()));
        }
        
        log.debug("Updated scores - Camera: {}, Battery: {}, Perf: {}, Looks: {}, Privacy: {}",
                 cameraScore, batteryScore, perfScore, looksScore, phone.getPrivacyScore());
        
        return phoneRepository.save(phone);
    }
    
    /**
     * Estimate build quality score based on brand and price
     */
    private int getBuildQualityScore(Phone phone) {
        int baseScore = 50;
        
        // Premium brands get higher base
        String brand = phone.getBrand().toLowerCase();
        if (brand.contains("apple") || brand.contains("samsung") && phone.getPrice() > 50000) {
            baseScore = 75;
        } else if (brand.contains("oneplus") || brand.contains("google") || brand.contains("motorola")) {
            baseScore = 65;
        } else if (brand.contains("xiaomi") || brand.contains("realme") || brand.contains("oppo") || brand.contains("vivo")) {
            baseScore = 55;
        }
        
        // Price-based adjustment
        if (phone.getPrice() > 80000) baseScore += 15;
        else if (phone.getPrice() > 60000) baseScore += 12;
        else if (phone.getPrice() > 40000) baseScore += 8;
        else if (phone.getPrice() > 25000) baseScore += 5;
        else if (phone.getPrice() > 15000) baseScore += 2;
        
        return Math.min(100, baseScore);
    }
    
    /**
     * Get privacy score based on brand
     */
    private int getPrivacyScore(String brand) {
        String lower = brand.toLowerCase();
        
        // Privacy-focused brands
        if (lower.contains("apple")) return 85;
        if (lower.contains("google")) return 75;
        if (lower.contains("samsung")) return 70;
        
        // Chinese brands (lower privacy due to data concerns)
        if (lower.contains("xiaomi") || lower.contains("oppo") || 
            lower.contains("vivo") || lower.contains("realme") || 
            lower.contains("oneplus")) {
            return 55;
        }
        
        // Other brands
        if (lower.contains("motorola") || lower.contains("nokia")) return 70;
        if (lower.contains("asus") || lower.contains("sony")) return 72;
        
        return 60; // Default
    }
    
    /**
     * Update a specific phone by ID
     */
    @Transactional
    public Phone updatePhoneById(Long phoneId) {
        Phone phone = phoneRepository.findById(phoneId)
            .orElseThrow(() -> new IllegalArgumentException("Phone not found: " + phoneId));
        
        return updatePhoneScores(phone);
    }
    
    /**
     * Update phones in a specific price range
     */
    @Transactional
    public UpdateResult updatePhonesByPriceRange(int minPrice, int maxPrice) {
        log.info("Updating phones in price range: ₹{} - ₹{}", minPrice, maxPrice);
        
        List<Phone> phones = phoneRepository.findByPriceBetween(minPrice, maxPrice);
        int updated = 0;
        int failed = 0;
        
        for (Phone phone : phones) {
            try {
                updatePhoneScores(phone);
                phoneRepository.save(phone);
                updated++;
            } catch (Exception e) {
                log.error("Failed to update phone {}: {}", phone.getId(), e.getMessage());
                failed++;
            }
        }
        
        log.info("Price range update complete. Updated: {}, Failed: {}", updated, failed);
        return new UpdateResult(updated, failed);
    }
    
    /**
     * Result class
     */
    public static class UpdateResult {
        private final int updated;
        private final int failed;
        
        public UpdateResult(int updated, int failed) {
            this.updated = updated;
            this.failed = failed;
        }
        
        public int getUpdated() { return updated; }
        public int getFailed() { return failed; }
        
        @Override
        public String toString() {
            return String.format("Updated: %d, Failed: %d, Total: %d", 
                               updated, failed, updated + failed);
        }
    }
}
