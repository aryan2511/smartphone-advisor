package com.phonepick.advisor.model;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    @NotNull(message = "Product type is required")
    private String productType;
    
    @NotNull(message = "Budget is required")
    private String budget;
    
    @NotNull(message = "Priorities are required")
    private Map<String, Integer> priorities;
    
    // Helper method to get budget range
    public BudgetRange getBudgetRange() {
        return switch (budget) {
            case "under-10" -> new BudgetRange(0, 10000);
            case "10-15" -> new BudgetRange(10000, 15000);
            case "15-20" -> new BudgetRange(15000, 20000);
            case "20-25" -> new BudgetRange(20000, 25000);
            case "25-30" -> new BudgetRange(25000, 30000);
            case "30-35" -> new BudgetRange(30000, 35000);
            case "35-40" -> new BudgetRange(35000, 40000);
            case "40-50" -> new BudgetRange(40000, 50000);
            case "50-60" -> new BudgetRange(50000, 60000);
            case "60-75" -> new BudgetRange(60000, 75000);
            case "75-plus" -> new BudgetRange(75000, 200000);
            case "New Gen" -> new BudgetRange(95000, 300000);
            default -> new BudgetRange(0, 200000);
        };
    }
    
    @Data
    @AllArgsConstructor
    public static class BudgetRange {
        private int min;
        private int max;
    }
}
