# Solution: YouTube Quota Limit Workaround

## Problem
YouTube Data API has strict quota limits (10,000 units/day). Each search = 100 units, so only 100 searches/day.

## Solution: Pre-Process & Cache Strategy

### Architecture
```
Manual URL Entry → Database Storage → Batch Transcript Fetch (Scheduled) → 
Sentiment Analysis (Scheduled) → Cached Results → Instant Recommendations
```

## Step 1: Database Setup

Run this in Neon PostgreSQL:

```sql
-- Create phone_reviews table
CREATE TABLE IF NOT EXISTS phone_reviews (
    id SERIAL PRIMARY KEY,
    phone_id BIGINT NOT NULL REFERENCES phones(id),
    channel_id BIGINT NOT NULL REFERENCES youtube_channels(id),
    video_id VARCHAR(20) NOT NULL,
    video_title VARCHAR(500),
    video_url VARCHAR(200) NOT NULL,
    transcript TEXT,
    sentiment_score INTEGER,
    analyzed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(phone_id, video_id)
);

CREATE INDEX idx_phone_reviews_phone ON phone_reviews(phone_id);
CREATE INDEX idx_phone_reviews_channel ON phone_reviews(channel_id);
```

## Step 2: Manual URL Entry (Quick Start)

Instead of searching YouTube via API, manually add video URLs:

### Find Videos Manually
1. Go to YouTube
2. Search: "Samsung Galaxy S24 review MKBHD"
3. Copy video URL: `https://www.youtube.com/watch?v=ABC123`
4. Extract video ID: `ABC123`

### Insert into Database
```sql
-- Example: Samsung Galaxy S24 review from MKBHD
INSERT INTO phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
VALUES (
    1, -- phone_id (check phones table)
    (SELECT id FROM youtube_channels WHERE channel_name = 'MKBHD'),
    'Zi8vJ_lMxQI', -- Video ID from URL
    'Samsung Galaxy S24 Ultra Review',
    'https://www.youtube.com/watch?v=Zi8vJ_lMxQI'
);

-- Add more reviews
INSERT INTO phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
VALUES (
    1,
    (SELECT id FROM youtube_channels WHERE channel_name = 'Mrwhosetheboss'),
    'XYZ789ABC12',
    'S24 Ultra: Best Phone of 2024?',
    'https://www.youtube.com/watch?v=XYZ789ABC12'
);
```

## Step 3: Batch Processing (Automated)

### How It Works
1. **Daily at 2 AM**: Fetch transcripts for videos (no YouTube Search API)
2. **Daily at 3 AM**: Analyze sentiment from transcripts
3. **Continuous**: Serve recommendations from cached data

### Configuration
Already configured in `application.properties`:
```properties
batch.processing.enabled=true
batch.transcript.cron=0 0 2 * * ?  # Daily at 2 AM
batch.sentiment.cron=0 0 3 * * ?   # Daily at 3 AM
```

### Manual Trigger (Testing)
```bash
# Process all pending transcripts and sentiment analysis
curl -X POST http://localhost:8080/api/admin/batch/process

# Update phone aggregate scores
curl -X POST http://localhost:8080/api/admin/batch/update-scores
```

## Step 4: Workflow

### Option A: Manual Entry (Recommended for 68 phones)
```
1. Find best YouTube reviews manually
2. Insert video URLs into database
3. Run batch job: curl -X POST http://localhost:8080/api/admin/batch/process
4. Transcripts fetched & analyzed automatically
5. Sentiment scores saved to database
6. Recommendations use cached scores
```

### Option B: Scheduled Automation
```
1. Add video URLs to database (manually or script)
2. Wait for scheduled jobs (2 AM, 3 AM)
3. System automatically fetches & analyzes
4. No manual intervention needed
```

## Step 5: Testing

### 1. Add Test Data
```sql
-- Get phone IDs
SELECT id, brand, model FROM phones LIMIT 10;

-- Add a review for phone ID 1
INSERT INTO phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
VALUES (
    1,
    (SELECT id FROM youtube_channels WHERE channel_name = 'MKBHD'),
    'Zi8vJ_lMxQI',
    'Test Review',
    'https://www.youtube.com/watch?v=Zi8vJ_lMxQI'
);
```

### 2. Trigger Batch Processing
```bash
curl -X POST http://localhost:8080/api/admin/batch/process
```

### 3. Check Results
```sql
-- Check if transcript was fetched
SELECT video_id, video_title, 
       LENGTH(transcript) as transcript_length,
       sentiment_score
FROM phone_reviews
WHERE phone_id = 1;

-- Check phone aggregate score
SELECT id, brand, model, youtube_sentiment_score
FROM phones
WHERE id = 1;
```

## Benefits of This Approach

### ✅ Advantages
1. **No quota issues**: Only uses TranscriptAPI (generous limits)
2. **Quality control**: Manually select best reviews
3. **Fast recommendations**: Instant (no API calls during user requests)
4. **Scalable**: Can pre-process all 68 phones gradually
5. **Reliable**: Not dependent on YouTube Search API availability

### ❌ No YouTube Search API Needed
- Skip video discovery via API
- Manually curate quality content
- More control over data quality

## CSV Helper Script (Optional)

Create a CSV with video URLs and import in bulk:

```csv
phone_id,channel_name,video_id,video_title,video_url
1,MKBHD,Zi8vJ_lMxQI,Samsung S24 Review,https://www.youtube.com/watch?v=Zi8vJ_lMxQI
1,Mrwhosetheboss,ABC123DEF45,S24 Ultimate Review,https://www.youtube.com/watch?v=ABC123DEF45
2,MKBHD,XYZ789GHI01,iPhone 15 Pro Max,https://www.youtube.com/watch?v=XYZ789GHI01
```

Then import:
```sql
COPY phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
FROM '/path/to/reviews.csv'
DELIMITER ','
CSV HEADER;
```

## Next Steps

1. ✅ Run database migration (create phone_reviews table)
2. ✅ Enable batch processing (already configured)
3. ⏳ Manually add 3-5 video URLs per phone (68 phones × 3 = ~200 URLs)
4. ⏳ Run batch process: `curl -X POST .../api/admin/batch/process`
5. ✅ System automatically maintains sentiment scores

## Timeline Estimate

- **Manual URL entry**: 30 minutes for 10 phones, ~3 hours for all 68
- **Batch processing**: Automatic (runs daily or on-demand)
- **Maintenance**: Zero (scheduled jobs handle everything)

## Alternative: Hybrid Approach

For new phones, use YouTube Search API sparingly:
- Only search when absolutely needed
- Cache results immediately
- Rely on batch jobs for maintenance

This keeps you well within quota limits!
