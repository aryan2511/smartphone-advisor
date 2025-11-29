package com.phonepick.advisor.config;

import com.phonepick.advisor.repository.PhoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final PhoneRepository phoneRepository;
    
    @Override
    public void run(String... args) {
        // Database initialization removed - phones should be imported via CSV sync
        long phoneCount = phoneRepository.count();
        log.info("Database contains {} phones", phoneCount);
        
        if (phoneCount == 0) {
            log.warn("No phones found in database. Please run the data-service sync-phones script to import phone data from CSV.");
        }
    }
}
