# Quick Reference: How Scoring Works

## Flow Overview

```
1. Phone added to database → Spec scores calculated
2. Batch jobs run (scheduled):
   - YouTube transcripts fetched (2-3 AM)
   - Feature sentiment analysis (per video)
   - Reddit posts analyzed (4-5 AM)
3. Sentiment scores stored in DB
4. User makes query → Unified scoring combines all data
5. Top 5 recommendations returned
```

---

## Current Configuration

### Trusted YouTube Channels
```java
MKBHD
Mrwhosetheboss
Trakin Tech
Geeky Ranjit
C4ETech
Beebom
TechBar
TechWiser
```

### Target Reddit Communities
```
r/Android
r/smartphones
r/PickAnAndroidForMe
r/AndroidQuestions
r/smartphone
r/IndianGaming
```

---

## Score Breakdown Example

**Phone: OnePlus 11R**

### 1. Spec Scores (PhoneSpecScoringService)
```
Camera:      85/100  (50MP + OIS + ultra-wide)
Battery:     82/100  (5000mAh + 100W charging)
Storage:     70/100  (256GB non-expandable)
Processor:   88/100  (Snapdragon 8+ Gen 1)
Display:     80/100  (AMOLED 120Hz)

Average Spec Score: 81/100
```

### 2. YouTube Analysis (FeatureSentimentAnalysisService)
```
MKBHD transcript:
  - Camera: 82 (positive: "excellent photos", "good zoom")
  - Battery: 78 (positive: "all day battery", "fast charging")
  - Performance: 90 (positive: "blazing fast", "no lag")

Mrwhosetheboss transcript:
  - Camera: 80
  - Battery: 75
  - Performance: 88

... (3 more reviewers)

Aggregate YouTube Score: 83/100
Consensus check: 4/5 positive → +3 bonus
Final YouTube: 86/100
```

### 3. Reddit Sentiment
```
Posts analyzed: 15
Positive mentions: 9
Negative mentions: 3
Neutral: 3

Reddit Score: 72/100
```

### 4. Unified Score Calculation
```
Weighted = (81 × 0.50) + (86 × 0.35) + (72 × 0.15)
         = 40.5 + 30.1 + 10.8
         = 81.4 ≈ 81/100
```

### 5. User Query Match
```
User priorities:
  Camera: 80 (rank 2)
  Battery: 90 (rank 1)
  Performance: 70 (rank 3)

Priority Match Score:
  = (82 × 0.90) + (85 × 0.80) + (88 × 0.70) / 3
  = 73.8 + 68 + 61.6 / 3
  = 67.8 ≈ 68/100
```

### 6. Final Recommendation Score
```
Final = (PriorityMatch × 0.70) + (Unified × 0.30)
      = (68 × 0.70) + (81 × 0.30)
      = 47.6 + 24.3
      = 71.9 ≈ 72/100
```

---

## Critical Rules

### Storage Scoring
```
✅ 128GB+ non-expandable:    Acceptable
✅ Any size + microSD:        Acceptable
❌ <128GB non-expandable:    MINIMUM SCORE (25)
```

### YouTube Consensus
```
Need: 3+ reviewers scoring 70+
Bonus: +3 points to unified score

Example:
Scores: [75, 78, 72, 65, 68]
Count 70+: 3 → ✅ BONUS

Scores: [75, 68, 65, 60, 72]
Count 70+: 2 → ❌ NO BONUS
```

### Feature Keywords Must Match
```
User priority: "camera"
Sentiment looks for: "camera", "photo", "picture", "video", etc.

User priority: "battery"
Sentiment looks for: "battery", "charge", "backup", "mah", etc.
```

---

## API Response Example

```json
{
  "recommendations": [
    {
      "id": 42,
      "brand": "OnePlus",
      "model": "11R",
      "price": 39999,
      "matchScore": 72,
      "cameraScore": 85,
      "batteryScore": 82,
      "softwareScore": 88,
      "youtubeSentimentScore": 86,
      "redditSentimentScore": 72,
      "insights": {
        "pros": ["Fastest charging in segment", "Flagship processor"],
        "cons": ["No wireless charging", "Average camera in low light"]
      },
      "comparisonReasons": [
        "vs Pixel 7a: ₹5,000 cheaper and noticeably faster performance",
        "vs Samsung S21 FE: Much longer battery life and better display"
      ]
    }
  ]
}
```

---

## Batch Job Schedule

### Nightly Operations (IST)
```
02:00 AM - YouTube transcript fetching
03:00 AM - Sentiment analysis on transcripts
04:00 AM - Reddit post scraping
05:00 AM - Reddit sentiment analysis
06:00 AM - Score updates in database
```

### Why Batch Processing?
- Avoids real-time API rate limits
- Pre-computed scores = fast recommendations
- Can handle 1000s of phones efficiently
- YouTube API quota: 10,000 units/day

---

## Weight Rationale

### Why 50-35-15 Split?

**50% Specs**
- Objective, measurable
- Always available
- No API dependencies
- Foundation of recommendation

**35% YouTube**
- Expert opinions
- Detailed feature analysis
- Trusted tech reviewers
- Consistent quality

**15% Reddit**
- Real user experiences
- Long-term ownership feedback
- Community consensus
- Can be more variable

### Why 70-30 Priority-Unified Split?

**70% User Priorities**
- User knows what they want
- Personalization is key
- Respects user choice

**30% Unified Score**
- Prevents bad recommendations
- Quality baseline
- Community validation

---

## Common Scenarios

### Scenario 1: Budget Phone Recommendation
```
Budget: ₹15K-₹20K
Priority: Battery > Camera > Performance

Process:
1. Filter 8 phones in budget
2. Battery scores matter most (90% weight)
3. Spec scores dominate (fewer reviews at budget tier)
4. Top pick: Redmi 13 (5000mAh, decent camera, ₹16,999)
```

### Scenario 2: Flagship Comparison
```
Budget: ₹70K-₹80K
Priority: Camera = Performance > Battery

Process:
1. Filter 4 flagship phones
2. All have extensive YouTube reviews
3. Consensus bonus matters
4. Top pick: Phone with 4/5 reviewer consensus on camera
```

### Scenario 3: Storage Dealbreaker
```
Option A: Amazing phone, 64GB non-expandable
Option B: Good phone, 128GB expandable

Result:
- Option A penalized (storage score: 25)
- Option B ranked higher despite lower other scores
- System protects user from bad long-term choice
```

---

## Debugging Tips

### Check Score Components
```sql
SELECT 
    model,
    camera_score,
    battery_score,
    youtube_sentiment_score,
    reddit_sentiment_score
FROM phones
WHERE model LIKE '%OnePlus%';
```

### Verify Batch Jobs Ran
```sql
-- Check if YouTube scores exist
SELECT COUNT(*) FROM phones WHERE youtube_sentiment_score IS NOT NULL;

-- Check if Reddit scores exist  
SELECT COUNT(*) FROM phones WHERE reddit_sentiment_score IS NOT NULL;
```

### Test Recommendation Logic
```java
// In RecommendationService
log.debug("Phone {}: base={}, unified={}, final={}",
    phone.getModel(), baseMatchScore, unifiedScore, finalMatchScore);
```

---

## Performance Metrics

### Database Queries
```
Budget filter:     ~50ms
Score calculation: ~5ms per phone
Total response:    <200ms for top 5
```

### Batch Processing
```
YouTube fetch:     ~30 phones/hour (API limits)
Sentiment analysis: ~100 phones/hour
Reddit scraping:   ~1000 posts/hour
```

### Accuracy Target
```
User satisfaction: 80%+ with top recommendation
Feature match:     90%+ with stated priorities
```

---

## Maintenance Checklist

### Weekly
- [ ] Monitor batch job logs
- [ ] Check API quotas (YouTube, Reddit)
- [ ] Verify new phones have scores

### Monthly
- [ ] Review consensus thresholds
- [ ] Update keyword lists if needed
- [ ] Add new trusted reviewers

### Quarterly
- [ ] Analyze recommendation accuracy
- [ ] Adjust weight distributions
- [ ] Update storage/battery thresholds
