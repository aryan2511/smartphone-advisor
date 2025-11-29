# Reddit Integration - Quick Start

## What Was Integrated?

### 1. Database
- New table: `reddit_posts` to store Reddit reviews
- Tracks: post content, engagement (upvotes/comments), sentiment scores
- Linked to existing phones table

### 2. Backend Services
- **RedditService**: Fetches posts from Reddit API
- **RedditBatchService**: Scheduled processing (daily at 4 AM fetch, 5 AM analysis)
- **RecommendationService**: Updated to include Reddit sentiment in match scores

### 3. REST API
- `POST /api/reddit/fetch` - Manual trigger fetch
- `POST /api/reddit/analyze` - Manual trigger analysis
- `GET /api/reddit/phone/{id}` - Get posts for phone
- `GET /api/reddit/phone/{id}/sentiment` - Get sentiment score
- `POST /api/reddit/search?phoneModel=...` - Search specific phone

### 4. Target Subreddits
Searches these communities:
- r/Android
- r/smartphones
- r/PickAnAndroidForMe
- r/AndroidQuestions
- r/smartphone
- r/IndianGaming

## Setup Instructions

### Step 1: Run Database Migration
```bash
# From backend directory
psql -h ep-withered-base-a1dbh9b8-pooler.ap-southeast-1.aws.neon.tech -U neondb_owner -d neondb -f database/reddit_migration.sql
```

Or execute the SQL in your database client.

### Step 2: Rebuild Backend
```bash
cd backend
mvn clean install
```

### Step 3: Start Backend
```bash
mvn spring-boot:run
```

## Testing

### Test 1: Manual Reddit Fetch
```bash
curl -X POST http://localhost:8080/api/reddit/fetch
```

### Test 2: Search Specific Phone
```bash
curl -X POST "http://localhost:8080/api/reddit/search?phoneModel=OnePlus 12"
```

### Test 3: Run Sentiment Analysis
```bash
curl -X POST http://localhost:8080/api/reddit/analyze
```

### Test 4: Check Sentiment Score
```bash
curl http://localhost:8080/api/reddit/phone/1/sentiment
```

### Test 5: Verify Recommendation Boost
```bash
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "budget": "20000-30000",
    "priorities": {
      "camera": 80,
      "battery": 60,
      "performance": 70,
      "privacy": 40,
      "looks": 50
    }
  }'
```

## How It Affects Recommendations

### Before Reddit Integration:
Match Score = Feature Score + YouTube Sentiment Boost

### After Reddit Integration:
Match Score = Feature Score + YouTube Sentiment Boost + **Reddit Sentiment Boost**

**Reddit Boost Calculation:**
- Average sentiment from all Reddit posts for the phone
- Formula: (reddit_score - 50) / 5
- Range: -10 to +10 points
- Example: 70 sentiment = +4 points, 30 sentiment = -4 points

## Scheduled Processing

### Daily Schedule:
- **2:00 AM** - YouTube transcript fetch
- **3:00 AM** - YouTube sentiment analysis  
- **4:00 AM** - Reddit post fetch (NEW)
- **5:00 AM** - Reddit sentiment analysis (NEW)

## Architecture

```
Frontend Request
    ↓
RecommendationController
    ↓
RecommendationService
    ↓ (calls)
    ├─ PhoneRepository (gets phones)
    ├─ YouTubeService (gets YouTube sentiment)
    └─ RedditService (gets Reddit sentiment) ← NEW
            ↓
        RedditPostRepository
```

## Rate Limits & Performance

### Reddit API (No Auth):
- ~60 requests/minute
- Current: 2-3 seconds between requests
- Safe for daily batch processing

### Processing Time Estimate:
- 68 phones × 6 subreddits × 3 seconds = ~20 minutes
- Runs once daily at 4 AM
- No impact on user experience

## Troubleshooting

### Issue: Posts not fetching
**Check:**
1. Internet connectivity
2. Reddit API accessibility
3. Logs: `tail -f backend/logs/application.log`

### Issue: No sentiment scores
**Solution:**
Run manual analysis:
```bash
curl -X POST http://localhost:8080/api/reddit/analyze
```

### Issue: Recommendations unchanged
**Verify:**
1. Posts exist: `SELECT COUNT(*) FROM reddit_posts;`
2. Scores calculated: `SELECT COUNT(*) FROM reddit_posts WHERE sentiment_score IS NOT NULL;`
3. Backend logs show Reddit service being called

## Next Steps

1. ✅ Reddit API integrated
2. ⏳ Run initial data fetch
3. ⏳ Verify sentiment analysis
4. ⏳ Test recommendation boosts
5. ⏳ Monitor scheduled jobs

## Notes

- Reddit data complements YouTube reviews
- Both sources contribute to final match score
- System gracefully handles missing data
- Rate limiting prevents API throttling
- Batch processing ensures scalability
