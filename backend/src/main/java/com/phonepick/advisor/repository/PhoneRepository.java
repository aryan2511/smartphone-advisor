package com.phonepick.advisor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.phonepick.advisor.model.Phone;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
    
    // Find phones within price range
    List<Phone> findByPriceBetween(Integer minPrice, Integer maxPrice);
    
    // Find all phones ordered by price
    List<Phone> findAllByOrderByPriceAsc();
    
    // Custom query to get phones by brand
    List<Phone> findByBrandIgnoreCase(String brand);
    
    // Find phones by model name (for Reddit/YouTube matching)
    List<Phone> findByModelContainingIgnoreCase(String model);
    
    // Find phones by brand and model
    List<Phone> findByBrandIgnoreCaseAndModelContainingIgnoreCase(String brand, String model);
    
    // Count phones in price range
    Long countByPriceBetween(Integer minPrice, Integer maxPrice);
    
    // Check if phone exists by brand and model (for duplicate detection)
    boolean existsByBrandAndModel(String brand, String model);
}
