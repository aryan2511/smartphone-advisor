## Database Setup

### Step 1: Run Migrations
1. Log in to your Neon PostgreSQL console: https://neon.tech
2. Navigate to your database
3. Run the SQL scripts in order:

**Script 1: Add sentiment score column**
```sql
ALTER TABLE phones 
ADD COLUMN IF NOT EXISTS youtube_sentiment_score INTEGER;
```

**Script 2: Insert YouTube channels**
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

## Backend Setup

### Step 2: Verify Configuration
Your `application.properties` already has:
```
youtube.api.key=AIzaSyDNAinEkAdTd0NKBRPVK6dlpzKbP8DpStE
youtube.transcript.api.url=https://youtube-transcript-api.fly.dev/transcript
youtube.transcript.api.key=sk_nXy9S4F51yxo1QA6-BfZglHp5vKh1IZNv6kZAEu9Sy8
```

### Step 3: Start Backend
```bash
cd backend
mvnw spring-boot:run
```

Backend will start on: http://localhost:8080

## API Endpoints

### Analyze Single Phone
```
POST http://localhost:8080/api/analysis/phone/{phoneId}
```

Example:
```bash
curl -X POST http://localhost:8080/api/analysis/phone/1
```

Response:
```json
{
  "phoneId": 1,
  "phoneModel": "Samsung Galaxy S24",
  "averageSentimentScore": 85,
  "channelScores": {
    "MKBHD": 90,
    "Mrwhosetheboss": 88,
    "Trakin Tech": 78
  },
  "message": "Analyzed 3 video(s) from credible YouTubers"
}
```

### Batch Analyze Phones
```
POST http://localhost:8080/api/analysis/phones
Body: [1, 2, 3, 4, 5]
```

## Frontend Integration

### Update Results Page
The results page needs to call the analysis endpoint when showing recommendations.

**File to modify:** `frontend/src/pages/Results.jsx`

Add before displaying recommendations:
```javascript
// After getting recommendations, analyze sentiment for top phones
const analyzeTopPhones = async (phones) => {
  const phoneIds = phones.slice(0, 5).map(p => p.id); // Top 5 phones
  
  try {
    const response = await fetch('http://localhost:8080/api/analysis/phones', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(phoneIds)
    });
    
    const results = await response.json();
    console.log('Sentiment analysis results:', results);
    
    // Update phone objects with sentiment scores
    phones.forEach(phone => {
      if (results[phone.id]) {
        phone.sentimentScore = results[phone.id].averageSentimentScore;
      }
    });
  } catch (error) {
    console.error('Failed to analyze phones:', error);
  }
};
```

## Testing Flow

1. Start backend: `cd backend && mvnw spring-boot:run`
2. Test single phone analysis: `curl -X POST http://localhost:8080/api/analysis/phone/1`
3. Check database to see updated `youtube_sentiment_score`
4. Integrate frontend to call endpoint

## How It Works

1. **Video Discovery**: System searches YouTube for reviews from 8 credible channels
2. **Transcript Fetching**: Downloads video transcripts via TranscriptAPI
3. **Sentiment Analysis**: Analyzes transcript for positive/negative words
4. **Score Calculation**: Averages scores from all channels (0-100 scale)
5. **Database Update**: Stores sentiment score in `phones.youtube_sentiment_score`
6. **Recommendation**: Uses sentiment score in recommendation algorithm

## Notes

- Sentiment scores: 0-50 (negative), 50 (neutral), 50-100 (positive)
- Analysis runs on-demand via API calls
- Results cached in database
- YouTube API has daily quota limits (10,000 units/day)
