# Phone Entity Changes Summary

## Overview
Updated the Phone entity and all related files to properly handle the new field structure and sentiment scores.

## Changes Made

### 1. Phone.java (Already Updated by User)
- Added `youtubeSentimentScore` field (Integer, nullable)
- Added `redditSentimentScore` field (Integer, nullable)
- Changed field names to match database columns:
  - `memory_and_storage` (stores RAM and Storage combined)
  - `displayInfo` (stores full display information)
  - `cameraInfo` (stores full camera information)

### 2. PhoneRecommendation.java
- Added `redditSentimentScore` field
- Updated `fromPhone()` method to:
  - Map `youtubeSentimentScore` from Phone entity
  - Map `redditSentimentScore` from Phone entity
  - Use correct field names: `displayInfo`, `cameraInfo`, `memory_and_storage`
  - Fixed specs map to use new field structure

### 3. RecommendationService.java
- Updated sentiment boost logic:
  - YouTube sentiment boost now uses `phone.getYoutubeSentimentScore()`
  - Reddit sentiment boost now uses `phone.getRedditSentimentScore()`
  - Both boosts applied to match scores (-10 to +10 range)

### 4. RedditBatchService.java
- Added `PhoneRepository` dependency
- Added `updatePhoneRedditScores()` method:
  - Calculates average Reddit sentiment for each phone
  - Updates `redditSentimentScore` field in Phone entity
- Scheduled to run after sentiment analysis completes

### 5. FlipkartDataParser.java
- Removed unused parsing methods:
  - `parseMemoryAndStorage()` - no longer needed
  - `extractDisplaySize()` - no longer needed
  - `parseCameraInfo()` - no longer needed
- Updated `parseLine()` method:
  - Store memory/storage as-is in `memory_and_storage`
  - Store display as-is in `displayInfo`
  - Store camera as-is in `cameraInfo`
- Updated `calculateFeatureScores()`:
  - Use `getCameraInfo()` instead of `getRearCamera()`
  - Initialize sentiment scores to `null` (batch jobs will populate)

### 6. Database Migration
Created: `/backend/src/main/resources/db/migration/V2__add_sentiment_scores.sql`
- Adds `youtube_sentiment_score` column (nullable, 0-100 range)
- Adds `reddit_sentiment_score` column (nullable, 0-100 range)
- Adds check constraints for valid score ranges

## Sentiment Score Flow

### YouTube Sentiment
1. `TranscriptBatchService` fetches transcripts (2 AM daily)
2. `TranscriptBatchService` analyzes sentiment (3 AM daily)
3. Sentiment stored in `PhoneReview` table
4. `updatePhoneAggregateScores()` calculates average
5. Average stored in `Phone.youtubeSentimentScore`

### Reddit Sentiment
1. `RedditBatchService` fetches posts (4 AM daily)
2. `RedditBatchService` analyzes sentiment (5 AM daily)
3. Sentiment stored in `RedditPost` table
4. `updatePhoneRedditScores()` calculates average
5. Average stored in `Phone.redditSentimentScore`

## How Sentiment Affects Recommendations

In `RecommendationService.getRecommendations()`:

```java
// Base match score calculated from feature priorities
int matchScore = calculateMatchScore(phone, request.getPriorities());

// YouTube sentiment boost: -10 to +10 points
if (phone.getYoutubeSentimentScore() != null) {
    int boost = (phone.getYoutubeSentimentScore() - 50) / 5;
    matchScore += boost;
}

// Reddit sentiment boost: -10 to +10 points
if (phone.getRedditSentimentScore() != null) {
    int boost = (phone.getRedditSentimentScore() - 50) / 5;
    matchScore += boost;
}

// Final match score: 0-100 (clamped)
```

## Testing Required

1. Test Flipkart data import:
   - POST `/api/analysis/import-flipkart`
   - Verify phones imported with correct field values
   - Verify sentiment scores initialized to `null`

2. Test batch jobs:
   - Trigger YouTube transcript fetch
   - Trigger sentiment analysis
   - Verify `youtubeSentimentScore` populated in phones table

3. Test Reddit integration:
   - Trigger Reddit post fetch
   - Trigger Reddit sentiment analysis
   - Verify `redditSentimentScore` populated in phones table

4. Test recommendations:
   - POST to `/api/recommendations` with user preferences
   - Verify sentiment scores included in response
   - Verify match scores adjusted by sentiment

## Next Steps

1. Run database migration (automatic on next startup with Hibernate)
2. Re-import phone data if needed
3. Run batch jobs to populate sentiment scores
4. Test frontend integration with new fields
