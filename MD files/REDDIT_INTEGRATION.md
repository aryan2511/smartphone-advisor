# Reddit API Integration

## Overview
The Reddit integration fetches user reviews and discussions about smartphones from relevant subreddits and performs sentiment analysis to enhance phone recommendations.

## Target Subreddits
- r/Android
- r/smartphones
- r/PickAnAndroidForMe
- r/AndroidQuestions
- r/smartphone
- r/IndianGaming

## Features
1. **Automated Reddit Post Fetching**: Searches Reddit for posts about each phone in the database
2. **Sentiment Analysis**: Analyzes post titles and content to extract sentiment scores
3. **Batch Processing**: Scheduled tasks run daily to keep data fresh
4. **Integration with Recommendations**: Reddit sentiment boosts phone match scores

## Scheduled Tasks
- **4:00 AM**: Fetch new Reddit posts for all phones
- **5:00 AM**: Analyze sentiment of unanalyzed posts

## Database Schema
```sql
CREATE TABLE reddit_posts (
    id BIGSERIAL PRIMARY KEY,
    phone_id BIGINT REFERENCES phones(id),
    post_id VARCHAR(20) UNIQUE,
    post_title VARCHAR(500),
    post_url VARCHAR(500),
    author VARCHAR(100),
    subreddit VARCHAR(100),
    content TEXT,
    upvotes INTEGER,
    num_comments INTEGER,
    sentiment_score INTEGER,
    post_created_at TIMESTAMP,
    analyzed_at TIMESTAMP,
    created_at TIMESTAMP
);
```

## API Endpoints

### Manual Fetch
```
POST /api/reddit/fetch
```
Manually triggers Reddit post fetching for all phones.

### Manual Analysis
```
POST /api/reddit/analyze
```
Manually triggers sentiment analysis for unanalyzed posts.

### Get Posts for Phone
```
GET /api/reddit/phone/{phoneId}
```
Returns all Reddit posts for a specific phone.

### Get Sentiment Score
```
GET /api/reddit/phone/{phoneId}/sentiment
```
Returns average sentiment score from Reddit posts.

### Search for Phone
```
POST /api/reddit/search?phoneModel={model}
```
Searches Reddit for posts about a specific phone model.

## How It Works

### 1. Post Fetching
- Searches each target subreddit for mentions of phone models
- Uses Reddit's JSON API (no authentication required)
- Stores post metadata, content, and engagement metrics
- Rate limited to 2 seconds between subreddits, 3 seconds between phones

### 2. Sentiment Analysis
- Combines post title and content
- Uses existing SentimentAnalysisService
- Stores score (0-100, where 50 is neutral)
- Only analyzes posts once

### 3. Recommendation Integration
- Calculates average sentiment from all Reddit posts
- Applies boost/penalty: (score - 50) / 5 = -10 to +10 points
- Combined with YouTube sentiment for final match score

## Configuration
Add to `application.properties`:
```properties
# Reddit batch processing
batch.reddit.fetch.cron=0 0 4 * * ?
batch.reddit.analysis.cron=0 0 5 * * ?
```

## Testing

### Test Reddit Fetch
```bash
curl -X POST http://localhost:8080/api/reddit/fetch
```

### Test Sentiment Analysis
```bash
curl -X POST http://localhost:8080/api/reddit/analyze
```

### Search for a Phone
```bash
curl -X POST "http://localhost:8080/api/reddit/search?phoneModel=iPhone 15 Pro"
```

### Get Phone's Reddit Score
```bash
curl http://localhost:8080/api/reddit/phone/1/sentiment
```

## Rate Limiting & Best Practices
- Reddit API rate limit: ~60 requests/minute for unauthenticated users
- Current implementation: ~2-3 seconds between requests
- Respects Reddit API guidelines with proper User-Agent header
- Avoids duplicate posts using unique post_id constraint

## Future Enhancements
- Add Reddit comment fetching and analysis
- Implement Reddit OAuth for higher rate limits
- Add real-time post monitoring for popular phones
- Filter posts by minimum upvotes/comments threshold
- Add sentiment trend analysis over time
