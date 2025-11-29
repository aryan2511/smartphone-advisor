package com.phonepick.advisor.model;

/**
 * Enum representing different sources of phone reviews
 */
public enum ReviewSourceType {
    REDDIT("reddit"),
    YOUTUBE("youtube"),
    AMAZON("amazon"),
    FLIPKART("flipkart");
    
    private final String value;
    
    ReviewSourceType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ReviewSourceType fromValue(String value) {
        for (ReviewSourceType type : ReviewSourceType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown review source type: " + value);
    }
}
