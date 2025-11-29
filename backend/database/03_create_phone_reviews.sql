-- Create table to store phone review videos (manual entry)
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

-- Example: Manually add video URLs for phones
-- Replace with actual YouTube URLs you find

-- Example for Samsung Galaxy S24
INSERT INTO phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
SELECT 
    1, -- phone_id for Samsung Galaxy S24
    id,
    'ABC123DEF45', -- Extract from youtube.com/watch?v=ABC123DEF45
    'Samsung Galaxy S24 Review',
    'https://www.youtube.com/watch?v=ABC123DEF45'
FROM youtube_channels WHERE channel_name = 'MKBHD'
ON CONFLICT (phone_id, video_id) DO NOTHING;

COMMENT ON TABLE phone_reviews IS 'Stores YouTube review videos for each phone with transcripts and sentiment analysis';
