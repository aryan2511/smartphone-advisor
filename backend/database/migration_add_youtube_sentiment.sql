-- Add youtube_sentiment_score column to phones table
-- This column stores the average sentiment score from YouTube video analysis (0-100)

ALTER TABLE phones 
ADD COLUMN IF NOT EXISTS youtube_sentiment_score INTEGER;

-- Add comment for documentation
COMMENT ON COLUMN phones.youtube_sentiment_score IS 'Average sentiment score from YouTube video reviews (0-100, where 50 is neutral)';
