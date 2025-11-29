# YouTube Integration Guide

## Components Added

1. **YouTubeService.java** - Searches videos and fetches transcripts
2. **SentimentAnalysisService.java** - Analyzes transcript sentiment
3. **PhoneAnalysisService.java** - Orchestrates analysis workflow
4. **PhoneAnalysisController.java** - REST endpoints for analysis
5. **RestClientConfig.java** - RestTemplate bean configuration

## Database Update

Run this SQL to add the sentiment score column:
```sql
ALTER TABLE phones ADD COLUMN youtube_sentiment_score INTEGER;
```

## Configuration

Add to application.properties:
```
youtube.api.key=YOUR_YOUTUBE_API_KEY
youtube.transcript.api.url=https://youtube-transcript-api.fly.dev/transcript
```

## API Endpoints

### Analyze Single Phone
```
POST /api/analysis/phone/{phoneId}
```

### Batch Analyze
```
POST /api/analysis/phones
Body: [1, 2, 3, 4, 5]
```

## How It Works

1. Backend receives phone selection from frontend
2. PhoneAnalysisService checks YouTube for videos from credible channels
3. Fetches video transcripts using YouTube Transcript API
4. SentimentAnalysisService analyzes transcripts (0-100 score)
5. Average sentiment stored in database
6. RecommendationService uses sentiment to boost/lower phone rankings

## Setup YouTube Channels

Insert credible YouTubers into database:
```sql
INSERT INTO youtube_channels (channel_id, channel_name, credibility_score, tier, active)
VALUES 
  ('UCbR6jJpva9VIIAHTse4C3hw', 'Mrwhosetheboss', 95, 1, true),
  ('UCWq-V_NHXgt_U-_sDLmNWXQ', 'Technical Guruji', 90, 1, true);
```
