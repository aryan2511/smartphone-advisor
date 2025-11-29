package com.phonepick.advisor.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.repository.PhoneRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlipkartDataParser {
    
    private final PhoneRepository phoneRepository;
    
    public Map<String, Object> parseAndSaveFlipkartData(String csvFilePath) {
        List<Phone> phones = new ArrayList<>();
        int totalLines = 0;
        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;
        Map<String, Integer> skipReasons = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String header = br.readLine(); // Read header
            log.info("CSV Header: {}", header);
            
            while ((line = br.readLine()) != null) {
                totalLines++;
                try {
                    Phone phone = parseLine(line);
                    if (phone != null) {
                        // Check if phone already exists
                        boolean exists = phoneRepository.existsByBrandAndModel(phone.getBrand(), phone.getModel());
                        if (exists) {
                            skipCount++;
                            skipReasons.merge("Duplicate (brand+model)", 1, Integer::sum);
                            log.debug("Skipping duplicate: {} {}", phone.getBrand(), phone.getModel());
                        } else {
                            try {
                                phoneRepository.save(phone);
                                successCount++;
                                if (successCount % 100 == 0) {
                                    log.info("Imported {} phones so far...", successCount);
                                }
                            } catch (DataIntegrityViolationException e) {
                                skipCount++;
                                skipReasons.merge("Database constraint violation", 1, Integer::sum);
                                log.warn("Constraint violation for: {} {}", phone.getBrand(), phone.getModel());
                            }
                        }
                    } else {
                        skipCount++;
                        skipReasons.merge("Parsing failed", 1, Integer::sum);
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error processing line {}: {}", totalLines, e.getMessage());
                }
            }
            
            log.info("Import completed: {} total, {} success, {} skipped, {} errors", 
                    totalLines, successCount, skipCount, errorCount);
            
        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage());
        }
        
        // Return statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLines", totalLines);
        stats.put("successfullyImported", successCount);
        stats.put("skipped", skipCount);
        stats.put("errors", errorCount);
        stats.put("skipReasons", skipReasons);
        
        return stats;
    }
    
    private Phone parseLine(String line) {
        // Split by comma but respect quoted strings
        List<String> fields = parseCSVLine(line);
        
        if (fields.size() < 9) {
            log.warn("Insufficient fields ({}) in line: {}", fields.size(), line);
            return null;
        }
        
        Phone phone = new Phone();
        
        try {
            // CSV format: model,price,memory_and_storage,display,camera,processor,battery,image,url
            
            // Parse title to extract brand and model
            String fullModel = fields.get(0).trim();
            parseBrandAndModel(fullModel, phone);
            
            // Parse price (remove ₹ and commas)
            String priceStr = fields.get(1).replace("₹", "").replace(",", "").trim();
            if (!priceStr.isEmpty()) {
                phone.setPrice(Integer.parseInt(priceStr));
            } else {
                log.warn("Empty price for: {}", fullModel);
                return null;
            }
            
            // Memory and Storage (e.g., "8 GB RAM | 128 GB ROM")
            String memoryStorage = fields.get(2).trim();
            phone.setMemory_and_storage(memoryStorage);
            
            // Display info (store as-is in new field)
            String displayInfo = fields.get(3).trim();
            phone.setDisplayInfo(displayInfo);
            
            // Camera (e.g., "50MP + 2MP | 8MP Front Camera")
            String cameraInfo = fields.get(4).trim();
            phone.setCameraInfo(cameraInfo);
            
            // Processor
            String processor = fields.get(5).trim();
            phone.setProcessor(processor);
            
            // Battery
            String battery = fields.get(6).trim();
            phone.setBattery(battery);
            
            // Image URL
            String imageUrl = fields.get(7).trim();
            phone.setImageUrl(imageUrl);
            
            // Flipkart URL
            String flipkartUrl = fields.get(8).trim();
            phone.setAffiliateFlipkart(flipkartUrl);
            
            // Calculate feature scores
            calculateFeatureScores(phone);
            
            return phone;
            
        } catch (Exception e) {
            log.error("Error parsing phone data: {}", e.getMessage());
            return null;
        }
    }
    
    private void parseBrandAndModel(String fullModel, Phone phone) {
        // Extract brand (first word)
        String[] parts = fullModel.split("\\s+", 2);
        if (parts.length > 0) {
            phone.setBrand(parts[0]);
            phone.setModel(parts.length > 1 ? parts[1] : parts[0]);
        } else {
            phone.setBrand("Unknown");
            phone.setModel(fullModel);
        }
    }
    

    
    private void calculateFeatureScores(Phone phone) {
        // Camera Score based on camera info
        phone.setCameraScore(calculateCameraScore(phone.getCameraInfo()));
        
        // Battery Score based on mAh
        phone.setBatteryScore(calculateBatteryScore(phone.getBattery()));
        
        // Software Score based on processor
        phone.setSoftwareScore(calculateSoftwareScore(phone.getProcessor()));
        
        // Privacy Score based on brand
        phone.setPrivacyScore(calculatePrivacyScore(phone.getBrand()));
        
        // Looks Score based on display info and processor
        phone.setLooksScore(calculateLooksScore(phone.getDisplayInfo(), phone.getProcessor()));
        
        // Initialize sentiment scores to null (will be populated by batch jobs)
        phone.setYoutubeSentimentScore(null);
        phone.setRedditSentimentScore(null);
    }
    
    private int calculateCameraScore(String camera) {
        if (camera == null) return 50;
        
        Pattern pattern = Pattern.compile("(\\d+)MP");
        Matcher matcher = pattern.matcher(camera);
        int maxMP = 0;
        
        while (matcher.find()) {
            int mp = Integer.parseInt(matcher.group(1));
            if (mp > maxMP) {
                maxMP = mp;
            }
        }
        
        if (maxMP >= 200) return 100;
        if (maxMP >= 108) return 95;
        if (maxMP >= 64) return 90;
        if (maxMP >= 50) return 85;
        if (maxMP >= 32) return 75;
        if (maxMP >= 13) return 60;
        return 50;
    }
    
    private int calculateBatteryScore(String battery) {
        if (battery == null) return 50;
        
        Pattern pattern = Pattern.compile("(\\d+)\\s*mAh");
        Matcher matcher = pattern.matcher(battery);
        if (matcher.find()) {
            int capacity = Integer.parseInt(matcher.group(1));
            
            if (capacity >= 7000) return 100;
            if (capacity >= 6500) return 95;
            if (capacity >= 6000) return 90;
            if (capacity >= 5500) return 85;
            if (capacity >= 5000) return 80;
            if (capacity >= 4500) return 70;
            if (capacity >= 4000) return 60;
            return 55;
        }
        return 50;
    }
    
    private int calculateSoftwareScore(String processor) {
        if (processor == null) return 60;
        
        String proc = processor.toLowerCase();
        
        // Flagship processors
        if (proc.contains("8 elite") || proc.contains("9300+")) return 100;
        if (proc.contains("8 gen 3") || proc.contains("9300")) return 98;
        if (proc.contains("8s gen 3") || proc.contains("dimensity 9")) return 95;
        
        // Upper mid-range
        if (proc.contains("7 gen 3") || proc.contains("7 gen 4") || proc.contains("dimensity 8")) return 88;
        if (proc.contains("7s gen 2") || proc.contains("dimensity 7")) return 82;
        
        // Mid-range
        if (proc.contains("6 gen") || proc.contains("dimensity 6")) return 75;
        
        // Entry-level
        if (proc.contains("4s") || proc.contains("helio") || proc.contains("t6") || proc.contains("t7")) return 65;
        
        return 60;
    }
    
    private int calculatePrivacyScore(String brand) {
        if (brand == null) return 70;
        
        String brandLower = brand.toLowerCase();
        
        if (brandLower.contains("iphone") || brandLower.contains("apple")) return 98;
        if (brandLower.contains("samsung") || brandLower.contains("google")) return 85;
        if (brandLower.contains("oneplus") || brandLower.contains("nothing")) return 80;
        if (brandLower.contains("motorola")) return 78;
        if (brandLower.contains("vivo") || brandLower.contains("oppo") || brandLower.contains("realme")) return 72;
        if (brandLower.contains("xiaomi") || brandLower.contains("redmi") || brandLower.contains("poco")) return 68;
        if (brandLower.contains("infinix") || brandLower.contains("lava")) return 70;
        
        return 70;
    }
    
    private int calculateLooksScore(String displayInfo, String processor) {
        int score = 70; // Base score
        
        if (displayInfo != null) {
            String display = displayInfo.toLowerCase();
            
            // Premium display types
            if (display.contains("amoled") || display.contains("oled")) score += 10;
            if (display.contains("ltpo")) score += 5;
            if (display.contains("super retina") || display.contains("fluid")) score += 5;
            if (display.contains("120hz") || display.contains("144hz")) score += 5;
            if (display.contains("fhd+") || display.contains("full hd+")) score += 3;
        }
        
        if (processor != null) {
            String proc = processor.toLowerCase();
            // Flagship processors suggest premium design
            if (proc.contains("8 gen 3") || proc.contains("8 elite") || proc.contains("9300")) score += 5;
        }
        
        return Math.min(100, score);
    }
    
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString().trim());
        return result;
    }
}
