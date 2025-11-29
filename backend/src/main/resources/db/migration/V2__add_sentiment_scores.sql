-- Add YouTube and Reddit sentiment score columns to phones table
-- These columns will be automatically populated by batch jobs

-- Add youtube_sentiment_score column if it doesn't exist
ALTER TABLE phones 
ADD COLUMN IF NOT EXISTS youtube_sentiment_score INTEGER;

-- Add reddit_sentiment_score column if it doesn't exist
ALTER TABLE phones 
ADD COLUMN IF NOT EXISTS reddit_sentiment_score INTEGER;

-- Add check constraints to ensure scores are between 0 and 100
ALTER TABLE phones 
ADD CONSTRAINT IF NOT EXISTS youtube_sentiment_score_range 
CHECK (youtube_sentiment_score IS NULL OR (youtube_sentiment_score >= 0 AND youtube_sentiment_score <= 100));

ALTER TABLE phones 
ADD CONSTRAINT IF NOT EXISTS reddit_sentiment_score_range 
CHECK (reddit_sentiment_score IS NULL OR (reddit_sentiment_score >= 0 AND reddit_sentiment_score <= 100));
