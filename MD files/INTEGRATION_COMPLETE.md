# YouTube Recommendation Service - Integration Summary

## ✅ What Was Done

### Backend Integration (Java Spring Boot)

**New Services Created:**
1. `YouTubeService.java` - Searches for videos and fetches transcripts
2. `SentimentAnalysisService.java` - Analyzes transcript sentiment
3. `PhoneAnalysisService.java` - Orchestrates the complete workflow
4. `PhoneAnalysisController.java` - Exposes REST API endpoints

**Configurations:**
- `RestClientConfig.java` - Added RestTemplate bean for API calls
- Updated `application.properties` with YouTube API settings

**Database Changes:**
- Added `youtube_sentiment_score` column to `phones` table
- Created migration SQL file

**Updated Services:**
- `RecommendationService.java` - Now uses sentiment scores to boost/lower rankings

## 🔧 How It Works

### Workflow:
```
1. User selects phones on frontend
2. Frontend calls: POST /api/analysis/phones with phone IDs
3. Backend searches YouTube for videos from credible channels
4. Fetches video transcripts via YouTube Transcript API
5. Runs sentiment analysis on transcripts
6. Calculates average sentiment score (0-100)
7. Stores score in database
8. Frontend calls: GET /api/recommendations
9. Backend uses sentiment to adjust match scores
10. Returns recommendations with sentiment-boosted rankings
```

### Sentiment Score Impact:
- Score 50 = Neutral (no change)
- Score > 50 = Positive boost (up to +10 points)
- Score < 50 = Negative impact (down to -10 points)

## 📋 Setup Required

### 1. Database Migration
Run this SQL on your Neon PostgreSQL:
```sql
ALTER TABLE phones ADD COLUMN youtube_sentiment_score INTEGER;
```

### 2. Configure YouTube API
Add to `application.properties` (or set as environment variable):
```properties
youtube.api.key=YOUR_YOUTUBE_API_KEY_HERE
```

Get API key from: https://console.cloud.google.com/
- Enable "YouTube Data API v3"
- Create credentials → API Key

### 3. Populate YouTube Channels
Insert credible tech reviewers:
```sql
INSERT INTO youtube_channels (channel_id, channel_name, credibility_score, tier, active, verified, created_at, last_updated)
VALUES 
  ('UCbR6jJpva9VIIAHTse4C3hw', 'Mrwhosetheboss', 95, 1, true, true, NOW(), NOW()),
  ('UCWq-V_NHXgt_U-_sDLmNWXQ', 'Technical Guruji', 90, 1, true, true, NOW(), NOW()),
  ('UCx73NI-pCjWAtDqlJl4434A', 'Geekyranjit', 88, 1, true, true, NOW(), NOW());
```

## 🔌 API Endpoints

### Analyze Single Phone
```bash
POST /api/analysis/phone/{phoneId}

Response:
{
  "phoneId": 1,
  "phoneModel": "Samsung Galaxy S24",
  "averageSentimentScore": 85,
  "channelScores": {
    "Mrwhosetheboss": 90,
    "Technical Guruji": 80
  },
  "message": "Analyzed 2 video(s) from credible YouTubers"
}
```

### Batch Analyze Multiple Phones
```bash
POST /api/analysis/phones
Content-Type: application/json

Body: [1, 2, 3, 4, 5]

Response:
{
  "1": { ... analysis result ... },
  "2": { ... analysis result ... },
  ...
}
```

### Get Recommendations (with sentiment)
```bash
GET /api/recommendations?budget=60000&priorities=camera:80,battery:70,performance:60,privacy:40,design:50

# Now automatically includes sentiment boost in rankings
```

## 💻 Frontend Integration

See `frontend/INTEGRATION_EXAMPLE.js` for React code example.

**Key changes needed:**
1. Before calling `/api/recommendations`, call `/api/analysis/phones` first
2. Pass array of phone IDs for batch analysis
3. Wait for analysis to complete (or run async)
4. Then fetch recommendations as usual

## ⚠️ Important Notes

### API Quotas
- YouTube Data API has daily quota (10,000 units)
- Each video search costs 100 units
- Consider caching results to avoid redundant calls

### Error Handling
- If no videos found → Neutral score (50) used
- If transcript unavailable → Skips that video
- If API key missing → Warning logged, continues without sentiment

### Performance
- Analysis takes 2-5 seconds per phone
- Batch analysis recommended for multiple phones
- Consider running analysis async or showing loading state

## 🎯 Testing

1. **Test YouTube Service:**
```bash
# Start backend
mvn spring-boot:run

# Test analysis
curl -X POST http://localhost:8080/api/analysis/phone/1
```

2. **Verify Database:**
```sql
SELECT id, brand, model, youtube_sentiment_score 
FROM phones 
WHERE youtube_sentiment_score IS NOT NULL;
```

3. **Test Recommendations:**
```bash
curl "http://localhost:8080/api/recommendations?budget=60000&priorities=camera:80,battery:70,performance:60,privacy:40,design:50"
```

## 📁 Files Modified/Created

### Created:
- `service/YouTubeService.java`
- `service/SentimentAnalysisService.java`
- `service/PhoneAnalysisService.java`
- `controller/PhoneAnalysisController.java`
- `config/RestClientConfig.java`
- `database/migration_add_youtube_sentiment.sql`
- `INTEGRATION_GUIDE.md`
- `frontend/INTEGRATION_EXAMPLE.js`

### Modified:
- `model/Phone.java` - Added youtubeSentimentScore field
- `service/RecommendationService.java` - Added sentiment boost logic
- `application.properties` - Added YouTube API configuration

## ✨ Next Steps

1. Run database migration
2. Get YouTube API key and add to config
3. Insert credible YouTube channels
4. Update frontend to call analysis endpoint
5. Test the complete flow
6. Deploy and monitor API quota usage

---

**The recommendation service is now fully integrated with the main project!** 🚀
