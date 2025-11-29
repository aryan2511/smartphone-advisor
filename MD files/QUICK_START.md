# Quick Start Guide - SmartPick Recommendation Service

## Prerequisites Checklist
- [x] YouTube API Key added to application.properties
- [ ] Database migration completed
- [ ] YouTube channels inserted
- [ ] Backend running
- [ ] Frontend running

## Step 1: Database Setup (5 minutes)

### Access Neon Console
1. Go to https://neon.tech
2. Login with your credentials
3. Select your database: `neondb`
4. Open SQL Editor

### Run Migration Scripts

**Script 1: Add Sentiment Score Column**
```sql
ALTER TABLE phones 
ADD COLUMN IF NOT EXISTS youtube_sentiment_score INTEGER;
```

**Script 2: Insert YouTube Channels**
```sql
INSERT INTO youtube_channels (channel_id, channel_name, subscriber_count, credibility_score, tier, language, focus_areas, verified, active) VALUES
('UCRiDxLhkhkfJK0KLcVuWK-Q', 'Mrwhosetheboss', 19500000, 95, 1, 'EN', '["smartphones", "tech_reviews", "comparisons"]', true, true),
('UCBJycsmduvYEL83R_U4JriQ', 'MKBHD', 19200000, 98, 1, 'EN', '["smartphones", "tech_reviews", "flagship_devices"]', true, true),
('UC3SEvBYhullC-aaETV4k2Mw', 'Trakin Tech', 8900000, 85, 2, 'EN', '["smartphones", "indian_market", "budget_phones"]', true, true),
('UC8vXJhMLjML0NVcujmjq64Q', 'Geeky Ranjit', 3100000, 82, 2, 'EN', '["smartphones", "tech_reviews", "indian_market"]', true, true),
('UCL4dFdrYeZnnPJx0JcdzMHw', 'C4ETech', 3800000, 80, 2, 'EN', '["smartphones", "detailed_reviews", "indian_market"]', true, true),
('UCpp6iYPrWHTIYQmrcCMyNhA', 'Beebom', 4200000, 78, 2, 'EN', '["smartphones", "tech_news", "how_to"]', true, true),
('UC-vHsnjFYm6b3OH3Su6KLEA', 'TechBar', 1200000, 75, 3, 'EN', '["smartphones", "reviews", "tech_tips"]', true, true),
('UCwVYGZMCy1pPi7aKSDK7Atw', 'TechWiser', 2100000, 76, 3, 'EN', '["smartphones", "tech_reviews", "comparisons"]', true, true)
ON CONFLICT (channel_id) DO NOTHING;
```

**Verify Installation**
```sql
-- Check if column exists
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'phones' AND column_name = 'youtube_sentiment_score';

-- Check channels
SELECT channel_name, credibility_score FROM youtube_channels;
```

## Step 2: Start Backend (2 minutes)

```bash
cd backend
mvnw spring-boot:run
```

Wait for: `Started AdvisorApplication in X seconds`

Backend URL: http://localhost:8080

## Step 3: Test API (1 minute)

### Test 1: Health Check
```bash
curl http://localhost:8080/api/phones
```

### Test 2: Analyze Single Phone
```bash
curl -X POST http://localhost:8080/api/analysis/phone/1
```

Expected Response:
```json
{
  "phoneId": 1,
  "phoneModel": "Samsung Galaxy S24",
  "averageSentimentScore": 85,
  "channelScores": {
    "MKBHD": 88,
    "Mrwhosetheboss": 90,
    "Trakin Tech": 78
  },
  "message": "Analyzed 3 video(s) from credible YouTubers"
}
```

### Test 3: Batch Analysis
```bash
curl -X POST http://localhost:8080/api/analysis/phones \
  -H "Content-Type: application/json" \
  -d "[1, 2, 3]"
```

## Step 4: Update Frontend (5 minutes)

### Location
File: `frontend/src/pages/Recommendations.jsx`

### Changes Needed
1. Add sentiment analysis API call
2. Display sentiment scores on phone cards

### Reference File
See: `frontend/SENTIMENT_INTEGRATION.js` for complete code

### Key Code Snippet
```javascript
// After fetching recommendations
const sentimentResponse = await fetch(`${API_BASE_URL}/analysis/phones`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(phoneIds)
});

const results = await sentimentResponse.json();

setRecommendations(prevPhones => 
  prevPhones.map(phone => ({
    ...phone,
    sentimentScore: results[phone.id]?.averageSentimentScore || null
  }))
);
```

## Step 5: Start Frontend (1 minute)

```bash
cd frontend
npm run dev
```

Frontend URL: http://localhost:5173

## Step 6: Test Complete Flow (2 minutes)

1. Open http://localhost:5173
2. Select "Smartphone"
3. Choose budget range
4. Set priorities (drag & drop)
5. View recommendations with sentiment scores

## Architecture Overview

```
User Input → Frontend → Backend API → Database
                ↓
         YouTube Search → Transcript API → Sentiment Analysis
                ↓
         Update Database → Return Enhanced Recommendations
```

## API Endpoints Summary

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/phones` | GET | Get all phones |
| `/api/recommend` | POST | Get recommendations |
| `/api/analysis/phone/{id}` | POST | Analyze single phone |
| `/api/analysis/phones` | POST | Batch analyze phones |

## Troubleshooting

### Backend won't start
- Check Java version: `java -version` (need Java 17+)
- Check database connection in application.properties
- Check port 8080 is available

### Frontend won't start
- Run: `npm install` in frontend folder
- Check port 5173 is available

### No sentiment scores
- Check YouTube API key is valid
- Check TranscriptAPI key is valid
- Check YouTube channels are inserted in database
- Check phone model names match YouTube video titles

### API returns empty results
- Verify phones exist in database
- Check backend logs for errors
- Test API directly with curl/Postman

## Configuration Files

### application.properties
```properties
youtube.api.key=YOUR_KEY_HERE
youtube.transcript.api.url=https://youtube-transcript-api.fly.dev/transcript
youtube.transcript.api.key=YOUR_KEY_HERE
```

### API Base URL (frontend)
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Next Steps

1. ✅ Database migration
2. ✅ Insert channels
3. ✅ Start backend
4. ✅ Test API
5. ✅ Update frontend
6. ✅ Start frontend
7. ⏳ Test complete flow
8. ⏳ Deploy to production

## Success Indicators

- Backend starts without errors
- API responds to test calls
- Frontend displays recommendations
- Sentiment scores show on phone cards
- Database has updated sentiment scores

## Support

If you encounter issues:
1. Check backend logs
2. Check frontend console (F12)
3. Verify database connection
4. Test API with curl/Postman
