-- Migration: Add YouTube sentiment score to phones table
-- Run this in your Neon PostgreSQL database console

ALTER TABLE phones 
ADD COLUMN IF NOT EXISTS youtube_sentiment_score INTEGER;

COMMENT ON COLUMN phones.youtube_sentiment_score IS 'Average sentiment score from YouTube video reviews (0-100, where 50 is neutral)';
