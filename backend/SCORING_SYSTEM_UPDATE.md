# Scoring System Updates

## Overview
Updated the SmartPick recommendation system with three key improvements:
1. Feature-based sentiment analysis (already implemented)
2. Enhanced storage scoring with strict penalties
3. Refined YouTube consensus bonus logic

---

## 1. Feature-Based Sentiment Analysis

### Implementation
**Service**: `FeatureSentimentAnalysisService.java`

### How It Works
Instead of generic positive/negative sentiment, reviews are analyzed per feature:

**Features Analyzed:**
- **Camera**: photo quality, video, zoom, low-light, portrait
- **Battery**: battery life, charging speed, backup time
- **Performance**: processor, gaming, multitasking, speed
- **Display**: brightness, refresh rate, colors, panel type
- **Design**: build quality, materials, feel, aesthetics

**Sentiment Keywords Per Feature:**

```java
Camera Positive: "excellent photo", "sharp images", "night mode", "good zoom"
Camera Negative: "poor camera", "blurry", "grainy", "washed out"

Battery Positive: "long battery", "all day battery", "fast charging"
Battery Negative: "battery drain", "heating", "dies quickly"

Performance Positive: "fast", "smooth", "no lag", "handles multitasking"
Performance Negative: "slow", "laggy", "stuttering", "frame drops"
```

### Score Calculation
For each feature:
1. Extract sentences mentioning the feature
2. Count positive vs negative keywords
3. Calculate score: `50 + (positiveRatio - 0.5) × 100`
4. Range: 0-100 (higher = better)

### Benefits
- **Scalable**: Easy to add new features
- **Precise**: User priorities directly match review categories
- **Transparent**: Clear why a phone scores high/low

---

## 2. Storage Scoring Rules

### Updated Logic in `PhoneSpecScoringService.java`

### Critical Rule
**Non-expandable storage < 128GB = MINIMUM SCORE (25 points)**

### Reasoning
- Modern apps require significant storage
- Photos/videos consume space quickly
- No expansion = stuck forever
- 128GB is the acceptable minimum without expansion

### Scoring Table

| Storage | Expandable | Non-Expandable | Notes |
|---------|-----------|----------------|-------|
| 1TB+    | 80 points | 80 points | Premium tier |
| 512GB   | 75 points | 75 points | High-end |
| 256GB   | 70 points | 70 points | Good |
| 128GB   | 67 points | 65 points | Minimum acceptable |
| 64GB    | 52 points | **25 points** | Only with expansion |
| 32GB    | 48 points | **25 points** | Bare minimum w/ expansion |

### Additional Bonuses
- **Expandable**: +5 points
- **UFS 4.0**: +8 points (faster read/write)
- **UFS 3.1**: +6 points
- **UFS 3.0**: +4 points

### Example Scenarios

```
Phone A: 64GB + microSD → Score: 52
Phone B: 64GB (no expansion) → Score: 25 ❌

Phone C: 128GB + microSD → Score: 67
Phone D: 128GB (no expansion) → Score: 65

Phone E: 256GB UFS 3.1 → Score: 76
```

---

## 3. YouTube Consensus Bonus

### Updated Logic in `UnifiedScoringService.java`

### The Formula
**Base Weights:**
- Spec Score: 50%
- YouTube Reviews: 35%
- Reddit: 15%

**Consensus Bonus:** +3 points if majority recommend

### Consensus Requirements

**Definition**: 3+ reviewers give 70+ score (positive recommendation)

**Thresholds:**
- **5-6 reviewers**: Need 3+ positive (≥50%)
- **3-4 reviewers**: Need 3+ positive (≥75%)

### Examples

```
Scenario 1: 5 reviewers
Scores: [75, 80, 72, 60, 55]
Positive count: 3 (75, 80, 72)
Result: ✅ Consensus bonus +3 points

Scenario 2: 6 reviewers  
Scores: [85, 78, 74, 68, 62, 45]
Positive count: 3 (85, 78, 74)
Result: ✅ Consensus bonus +3 points

Scenario 3: 4 reviewers
Scores: [75, 72, 68, 60]
Positive count: 2 (only 75, 72 above 70)
Result: ❌ No consensus (need 3+)

Scenario 4: 5 reviewers
Scores: [82, 79, 65, 58, 52]
Positive count: 2 (82, 79)
Result: ❌ No consensus (need 3+)
```

### Why This Approach?

1. **Fair to phones**: Single negative review won't kill consensus
2. **Quality signal**: Majority agreement = reliable recommendation
3. **Scalable**: Works with 3-6 reviewers (your trusted list)
4. **Modest bonus**: +3 points validates, doesn't dominate

---

## 4. Complete Scoring Flow

### Step-by-Step Process

```
User Query:
  Budget: ₹30K-₹40K
  Priorities: camera (100), battery (80), performance (60)
  
↓

1. Filter phones in budget
   → Found: 15 phones

↓

2. Calculate spec scores (PhoneSpecScoringService)
   Phone X:
     Camera spec: 85 (108MP + OIS + night mode)
     Battery spec: 78 (5000mAh + 67W charging)
     Storage spec: 70 (256GB, non-expandable)
     Processor: 82 (Snapdragon 7+ Gen 2)
     Display: 75 (AMOLED, 120Hz)
   → Average spec score: 78

↓

3. Get YouTube sentiment (FeatureSentimentAnalysisService)
   Reviewers: [MKBHD, Mrwhosetheboss, Trakin Tech, C4ETech, Geeky Ranjit]
   Feature scores per reviewer:
     Camera: [82, 78, 85, 75, 80] → Avg: 80
     Battery: [70, 75, 72, 68, 74] → Avg: 72
     Performance: [85, 88, 82, 79, 83] → Avg: 83
   
   Individual scores: [80, 82, 78, 75, 79]
   Consensus check: 3/5 gave 78+ → ✅ Bonus +3
   → YouTube score: 79 + 3 = 82

↓

4. Get Reddit sentiment (if available)
   → Reddit score: 68

↓

5. Calculate unified score (UnifiedScoringService)
   Unified = (78 × 0.50) + (82 × 0.35) + (68 × 0.15)
   Unified = 39 + 28.7 + 10.2 = 77.9 ≈ 78

↓

6. Calculate priority match (RecommendationService)
   Match = (85 × 1.0) + (78 × 0.8) + (82 × 0.6) / 3
   Match = 85 + 62.4 + 49.2 / 3 = 65.5 ≈ 66

↓

7. Final recommendation score
   Final = (66 × 0.70) + (78 × 0.30)
   Final = 46.2 + 23.4 = 69.6 ≈ 70

↓

8. Rank and return top 5
```

---

## 5. Database Schema

### Relevant Fields in `phones` table:

```sql
-- Spec-based scores (calculated from specs)
camera_score INTEGER
battery_score INTEGER
software_score INTEGER  -- Performance
privacy_score INTEGER
looks_score INTEGER

-- Review sentiment scores (from analysis)
youtube_sentiment_score INTEGER
reddit_sentiment_score INTEGER

-- These are populated by batch jobs
```

---

## 6. Key Service Files

### File Structure
```
backend/src/main/java/com/phonepick/advisor/service/
├── FeatureSentimentAnalysisService.java    ← Feature-based sentiment
├── PhoneSpecScoringService.java            ← Spec scoring (updated)
├── UnifiedScoringService.java              ← Combines all scores (updated)
├── RecommendationService.java              ← User-facing recommendations
├── YouTubeService.java                     ← Fetches transcripts
├── TranscriptBatchService.java             ← Batch processing
└── PhoneScoreUpdateService.java            ← DB updates
```

---

## 7. What Changed?

### Files Modified:
1. ✅ `PhoneSpecScoringService.java` - Storage scoring rules
2. ✅ `UnifiedScoringService.java` - YouTube consensus logic

### Files Already Implemented:
- ✅ `FeatureSentimentAnalysisService.java` - Feature-based analysis
- ✅ Database schema with sentiment score columns

### No Changes Needed:
- ❌ Batch services (already running)
- ❌ YouTube/Reddit integration (already working)
- ❌ Frontend (uses existing API)

---

## 8. Testing Scenarios

### Scenario 1: Storage Penalty
```
Phone: Redmi Note 13
Storage: 64GB (non-expandable)
Expected: Storage score = 25 (minimum)
Impact: Lower overall recommendation score
```

### Scenario 2: YouTube Consensus
```
Phone: Nothing Phone 2
Reviewers: 5 trusted channels
Scores: [82, 79, 77, 65, 72]
Count 70+: 4 reviewers
Expected: ✅ Consensus bonus +3 points
```

### Scenario 3: Feature Match
```
User priority: Camera (100), Battery (60)
Phone A: Camera 90, Battery 60
Phone B: Camera 75, Battery 85

Analysis:
- Phone A better matches user's TOP priority (camera)
- Phone B has higher total, but wrong emphasis
Expected: Phone A ranks higher
```

---

## 9. Future Enhancements

### Potential Additions:
1. **More features**: 5G support, NFC, IR blaster
2. **Price trends**: Amazon/Flipkart price history
3. **User feedback**: Community votes on recommendations
4. **ML model**: Learn user preferences over time

### Easy to Scale:
- Add new feature to `FEATURE_PATTERNS` map
- Add keywords for that feature
- System automatically incorporates it

---

## Summary

✅ **Feature-based sentiment** - Already implemented
✅ **Storage penalties** - Updated, strict minimum for non-expandable
✅ **YouTube consensus** - Refined logic, +3 bonus for 3+ positive reviews

The system now provides:
- More accurate storage assessments
- Better validation through reviewer consensus  
- Scalable architecture for new features
- Fair weighting between specs and reviews
