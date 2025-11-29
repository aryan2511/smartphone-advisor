package com.phonepick.advisor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced phone specification scoring service
 * Provides granular, feature-based scoring for phone specifications
 */
@Service
@Slf4j
public class PhoneSpecScoringService {
    
    /**
     * Score camera specifications
     * Considers: MP count, number of cameras, special features
     */
    public int scoreCameraSpec(String cameraInfo) {
        if (cameraInfo == null || cameraInfo.isEmpty()) {
            return 50;
        }
        
        int score = 40; // Base score
        String lower = cameraInfo.toLowerCase();
        
        // Extract main camera MP
        Pattern mpPattern = Pattern.compile("(\\d+)\\s*mp");
        Matcher matcher = mpPattern.matcher(lower);
        
        int maxMp = 0;
        while (matcher.find()) {
            int mp = Integer.parseInt(matcher.group(1));
            maxMp = Math.max(maxMp, mp);
        }
        
        // Score based on MP (diminishing returns after 108MP)
        if (maxMp >= 200) score += 30;
        else if (maxMp >= 108) score += 25;
        else if (maxMp >= 64) score += 20;
        else if (maxMp >= 50) score += 15;
        else if (maxMp >= 48) score += 12;
        else if (maxMp >= 32) score += 10;
        else if (maxMp >= 16) score += 5;
        
        // Bonus for multiple cameras
        long cameraCount = lower.split("\\+").length;
        if (cameraCount >= 4) score += 10;
        else if (cameraCount >= 3) score += 7;
        else if (cameraCount >= 2) score += 4;
        
        // Bonus for special features
        if (lower.contains("ois") || lower.contains("optical stabilization")) score += 5;
        if (lower.contains("telephoto") || lower.contains("periscope")) score += 5;
        if (lower.contains("ultra wide") || lower.contains("ultrawide")) score += 3;
        if (lower.contains("macro")) score += 2;
        if (lower.contains("night mode") || lower.contains("night sight")) score += 3;
        
        return Math.min(100, score);
    }
    
    /**
     * Score battery specifications with granular 250mAh increments
     * Also considers fast charging
     */
    public int scoreBatterySpec(String batteryInfo) {
        if (batteryInfo == null || batteryInfo.isEmpty()) {
            return 50;
        }
        
        int score = 30; // Base score
        String lower = batteryInfo.toLowerCase();
        
        // Extract battery capacity
        Pattern capacityPattern = Pattern.compile("(\\d+)\\s*mah");
        Matcher matcher = capacityPattern.matcher(lower);
        
        if (matcher.find()) {
            int capacity = Integer.parseInt(matcher.group(1));
            
            // Granular scoring in 250mAh increments
            if (capacity >= 6000) score += 40;
            else if (capacity >= 5750) score += 38;
            else if (capacity >= 5500) score += 36;
            else if (capacity >= 5250) score += 34;
            else if (capacity >= 5000) score += 32;
            else if (capacity >= 4750) score += 28;
            else if (capacity >= 4500) score += 24;
            else if (capacity >= 4250) score += 20;
            else if (capacity >= 4000) score += 16;
            else if (capacity >= 3750) score += 12;
            else if (capacity >= 3500) score += 8;
            else if (capacity >= 3250) score += 4;
        }
        
        // Fast charging bonus
        Pattern chargingPattern = Pattern.compile("(\\d+)\\s*w");
        Matcher chargingMatcher = chargingPattern.matcher(lower);
        
        if (chargingMatcher.find()) {
            int wattage = Integer.parseInt(chargingMatcher.group(1));
            
            if (wattage >= 120) score += 15;
            else if (wattage >= 80) score += 12;
            else if (wattage >= 65) score += 10;
            else if (wattage >= 45) score += 7;
            else if (wattage >= 30) score += 5;
            else if (wattage >= 18) score += 3;
        }
        
        // Wireless charging bonus
        if (lower.contains("wireless")) score += 5;
        if (lower.contains("reverse charging")) score += 3;
        
        return Math.min(100, score);
    }
    
    /**
     * Score storage and RAM with emphasis on expandability
     * CRITICAL: Non-expandable storage below 128GB gets minimum score
     */
    public int scoreStorageAndRam(String memoryInfo) {
        if (memoryInfo == null || memoryInfo.isEmpty()) {
            return 50;
        }
        
        int score = 30; // Base score
        String lower = memoryInfo.toLowerCase();
        
        // Extract RAM
        Pattern ramPattern = Pattern.compile("(\\d+)\\s*gb\\s*ram");
        Matcher ramMatcher = ramPattern.matcher(lower);
        
        if (ramMatcher.find()) {
            int ram = Integer.parseInt(ramMatcher.group(1));
            
            if (ram >= 16) score += 20;
            else if (ram >= 12) score += 17;
            else if (ram >= 8) score += 14;
            else if (ram >= 6) score += 10;
            else if (ram >= 4) score += 6;
            else if (ram >= 3) score += 3;
        }
        
        // Extract storage
        Pattern storagePattern = Pattern.compile("(\\d+)\\s*gb(?!\\s*ram)");
        Matcher storageMatcher = storagePattern.matcher(lower);
        
        int maxStorage = 0;
        while (storageMatcher.find()) {
            int storage = Integer.parseInt(storageMatcher.group(1));
            maxStorage = Math.max(maxStorage, storage);
        }
        
        // Check expandability
        boolean expandable = lower.contains("expandable") || 
                           lower.contains("card slot") || 
                           lower.contains("microsd");
        
        // CRITICAL RULE: Non-expandable < 128GB gets minimum score
        if (!expandable && maxStorage < 128) {
            log.warn("Storage penalty: {}GB non-expandable < 128GB - minimum score", maxStorage);
            return 25; // Minimum score for insufficient non-expandable storage
        }
        
        // Storage scoring with scalability
        if (maxStorage >= 1024) score += 30; // 1TB+
        else if (maxStorage >= 512) score += 25;
        else if (maxStorage >= 256) score += 20;
        else if (maxStorage >= 128) {
            score += expandable ? 17 : 15; // Slight bonus for expandable
        }
        else if (maxStorage >= 64 && expandable) {
            score += 12; // Only acceptable with expansion
        }
        else if (maxStorage >= 32 && expandable) {
            score += 8; // Bare minimum with expansion
        }
        
        // Bonus for expandability
        if (expandable) score += 5;
        
        // UFS version bonus (faster storage)
        if (lower.contains("ufs 4.0")) score += 8;
        else if (lower.contains("ufs 3.1")) score += 6;
        else if (lower.contains("ufs 3.0")) score += 4;
        
        return Math.min(100, score);
    }
    
    /**
     * Score processor/performance
     */
    public int scoreProcessor(String processorInfo) {
        if (processorInfo == null || processorInfo.isEmpty()) {
            return 50;
        }
        
        int score = 40; // Base score
        String lower = processorInfo.toLowerCase();
        
        // Flagship processors
        if (lower.contains("snapdragon 8 gen 3") || lower.contains("sd 8 gen 3")) {
            score += 30;
        } else if (lower.contains("snapdragon 8 gen 2") || lower.contains("sd 8 gen 2")) {
            score += 28;
        } else if (lower.contains("snapdragon 8+ gen 1") || lower.contains("sd 8+ gen 1")) {
            score += 26;
        } else if (lower.contains("snapdragon 8 gen 1") || lower.contains("sd 8 gen 1")) {
            score += 24;
        } else if (lower.contains("snapdragon 888")) {
            score += 22;
        } else if (lower.contains("dimensity 9200") || lower.contains("dimensity 9300")) {
            score += 28;
        } else if (lower.contains("dimensity 9000")) {
            score += 26;
        } else if (lower.contains("exynos 2400")) {
            score += 25;
        } else if (lower.contains("exynos 2200")) {
            score += 23;
        }
        // Mid-range processors
        else if (lower.contains("snapdragon 7+ gen 3") || lower.contains("snapdragon 7s gen 3")) {
            score += 20;
        } else if (lower.contains("snapdragon 7+ gen 2") || lower.contains("snapdragon 7s gen 2")) {
            score += 18;
        } else if (lower.contains("snapdragon 778") || lower.contains("snapdragon 780")) {
            score += 16;
        } else if (lower.contains("dimensity 8200") || lower.contains("dimensity 8300")) {
            score += 18;
        } else if (lower.contains("dimensity 7200")) {
            score += 15;
        }
        // Budget processors
        else if (lower.contains("snapdragon 6") || lower.contains("snapdragon 4")) {
            score += 10;
        } else if (lower.contains("dimensity 6")) {
            score += 10;
        } else if (lower.contains("helio g")) {
            score += 8;
        }
        
        // Process node bonus
        if (lower.contains("3nm") || lower.contains("3 nm")) score += 10;
        else if (lower.contains("4nm") || lower.contains("4 nm")) score += 8;
        else if (lower.contains("5nm") || lower.contains("5 nm")) score += 6;
        else if (lower.contains("6nm") || lower.contains("6 nm")) score += 4;
        
        return Math.min(100, score);
    }
    
    /**
     * Score display specifications
     */
    public int scoreDisplay(String displayInfo) {
        if (displayInfo == null || displayInfo.isEmpty()) {
            return 50;
        }
        
        int score = 35; // Base score
        String lower = displayInfo.toLowerCase();
        
        // Panel type
        if (lower.contains("amoled") || lower.contains("oled") || lower.contains("super amoled")) {
            score += 15;
        } else if (lower.contains("ips lcd") || lower.contains("lcd")) {
            score += 8;
        }
        
        // Refresh rate
        if (lower.contains("144hz") || lower.contains("144 hz")) score += 15;
        else if (lower.contains("120hz") || lower.contains("120 hz")) score += 12;
        else if (lower.contains("90hz") || lower.contains("90 hz")) score += 8;
        else if (lower.contains("60hz") || lower.contains("60 hz")) score += 3;
        
        // Resolution
        if (lower.contains("2k") || lower.contains("1440p") || lower.contains("quad hd")) {
            score += 12;
        } else if (lower.contains("fhd+") || lower.contains("1080p") || lower.contains("full hd")) {
            score += 8;
        } else if (lower.contains("hd+")) {
            score += 4;
        }
        
        // Size bonus for larger screens
        Pattern sizePattern = Pattern.compile("(\\d+\\.\\d+)");
        Matcher sizeMatcher = sizePattern.matcher(lower);
        if (sizeMatcher.find()) {
            double size = Double.parseDouble(sizeMatcher.group(1));
            if (size >= 6.7) score += 8;
            else if (size >= 6.5) score += 6;
            else if (size >= 6.0) score += 4;
        }
        
        // Special features
        if (lower.contains("ltpo") || lower.contains("adaptive refresh")) score += 5;
        if (lower.contains("hdr10+") || lower.contains("hdr10")) score += 5;
        if (lower.contains("dolby vision")) score += 5;
        if (lower.contains("gorilla glass") || lower.contains("victus")) score += 3;
        
        return Math.min(100, score);
    }
}
