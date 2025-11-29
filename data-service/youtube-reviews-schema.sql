-- ============================================
-- YouTube Reviews System
-- Store sentiment analysis from YouTube reviews
-- ============================================

-- Table: youtube_reviews
-- Stores analyzed reviews from YouTube videos
CREATE TABLE IF NOT EXISTS youtube_reviews (
    id BIGSERIAL PRIMARY KEY,
    phone_id BIGINT NOT NULL REFERENCES phones(id) ON DELETE CASCADE,
    
    -- Video metadata
    video_id VARCHAR(20) UNIQUE NOT NULL, -- YouTube video ID (e.g., "dQw4w9WgXcQ")
    channel_name VARCHAR(100) NOT NULL,
    channel_id VARCHAR(50),
    video_title TEXT NOT NULL,
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    
    -- Video stats
    view_count BIGINT,
    like_count INTEGER,
    published_at TIMESTAMP,
    
    -- Sentiment Analysis Results
    sentiment_score INTEGER CHECK (sentiment_score >= -100 AND sentiment_score <= 100),
    -- -100 to -50: Very Negative
    -- -49 to -1: Negative
    -- 0: Neutral
    -- 1 to 49: Positive
    -- 50 to 100: Very Positive
    
    -- Extracted Insights (Arrays for easy querying)
    positive_points TEXT[], -- ["Great camera", "Long battery life"]
    negative_points TEXT[], -- ["Expensive", "Heavy"]
    
    -- Summary
    key_insights TEXT, -- Brief summary of the review
    recommendation TEXT, -- "Recommended" / "Not Recommended" / "Mixed"
    
    -- Transcript metadata
    transcript_language VARCHAR(10) DEFAULT 'en',
    transcript_available BOOLEAN DEFAULT true,
    
    -- Processing metadata
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT unique_phone_video UNIQUE(phone_id, video_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_youtube_reviews_phone_id ON youtube_reviews(phone_id);
CREATE INDEX IF NOT EXISTS idx_youtube_reviews_channel ON youtube_reviews(channel_name);
CREATE INDEX IF NOT EXISTS idx_youtube_reviews_sentiment ON youtube_reviews(sentiment_score DESC);
CREATE INDEX IF NOT EXISTS idx_youtube_reviews_published ON youtube_reviews(published_at DESC);


-- Table: trusted_youtubers
-- List of tech reviewers we trust
CREATE TABLE IF NOT EXISTS trusted_youtubers (
    id SERIAL PRIMARY KEY,
    channel_id VARCHAR(50) UNIQUE NOT NULL,
    channel_name VARCHAR(100) NOT NULL,
    subscriber_count BIGINT,
    
    -- Credibility metrics
    trustworthiness_score INTEGER CHECK (trustworthiness_score >= 1 AND trustworthiness_score <= 10),
    specialization VARCHAR(50), -- 'smartphones', 'tech', 'budget', 'gaming'
    
    -- Active status
    is_active BOOLEAN DEFAULT true,
    last_checked TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert popular Indian tech YouTubers
INSERT INTO trusted_youtubers (channel_id, channel_name, trustworthiness_score, specialization) VALUES
    ('UCXGgrKt94gR6lmN4aN3mYTg', 'Technical Guruji', 9, 'smartphones'),
    ('UCOhHO2ICt0ti9KAh-QHvttQ', 'Mrwhosetheboss', 10, 'smartphones'),
    ('UCBJycsmduvYEL83R_U4JriQ', 'Marques Brownlee (MKBHD)', 10, 'tech'),
    ('UC7cs6Hdf2JWPV_rRgvg-wSg', 'Trakin Tech', 8, 'smartphones'),
    ('UCf_suVenvfMZ4JYSbmalKNQ', 'Geeky Ranjit', 9, 'smartphones'),
    ('UCYSt6V_ta00dS_g52MliaIg', 'C4ETech', 8, 'tech'),
    ('UCDLUxbvomVR-TdBnLXM4p3Q', 'Beebom', 7, 'smartphones'),
    ('UCxvLs6GdK4HLj4JOoGqvsLg', 'TechBar', 8, 'smartphones')
ON CONFLICT (channel_id) DO NOTHING;


-- Table: youtube_sync_log
-- Track when we last synced reviews for each phone
CREATE TABLE IF NOT EXISTS youtube_sync_log (
    id BIGSERIAL PRIMARY KEY,
    phone_id BIGINT REFERENCES phones(id) ON DELETE CASCADE,
    
    videos_found INTEGER DEFAULT 0,
    videos_processed INTEGER DEFAULT 0,
    videos_failed INTEGER DEFAULT 0,
    
    sync_started_at TIMESTAMP,
    sync_completed_at TIMESTAMP,
    status VARCHAR(20), -- 'pending', 'processing', 'completed', 'failed'
    error_message TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sync_log_phone_id ON youtube_sync_log(phone_id);


-- ============================================
-- Views for Easy Querying
-- ============================================

-- View: Phone reviews summary
CREATE OR REPLACE VIEW phone_review_summary AS
SELECT 
    p.id as phone_id,
    p.brand,
    p.model,
    COUNT(yr.id) as review_count,
    ROUND(AVG(yr.sentiment_score)) as avg_sentiment,
    COUNT(CASE WHEN yr.sentiment_score >= 50 THEN 1 END) as very_positive_count,
    COUNT(CASE WHEN yr.sentiment_score >= 1 AND yr.sentiment_score < 50 THEN 1 END) as positive_count,
    COUNT(CASE WHEN yr.sentiment_score < 0 AND yr.sentiment_score > -50 THEN 1 END) as negative_count,
    COUNT(CASE WHEN yr.sentiment_score <= -50 THEN 1 END) as very_negative_count,
    MAX(yr.published_at) as latest_review_date
FROM phones p
LEFT JOIN youtube_reviews yr ON p.id = yr.phone_id
GROUP BY p.id, p.brand, p.model;


-- View: Top reviewed phones
CREATE OR REPLACE VIEW top_reviewed_phones AS
SELECT 
    p.brand || ' ' || p.model as phone_name,
    COUNT(yr.id) as review_count,
    ROUND(AVG(yr.sentiment_score)) as avg_sentiment,
    ROUND(AVG(yr.view_count)) as avg_views
FROM phones p
JOIN youtube_reviews yr ON p.id = yr.phone_id
GROUP BY p.id, p.brand, p.model
ORDER BY review_count DESC, avg_sentiment DESC
LIMIT 20;


-- View: Recent positive reviews
CREATE OR REPLACE VIEW recent_positive_reviews AS
SELECT 
    p.brand || ' ' || p.model as phone_name,
    yr.channel_name,
    yr.video_title,
    yr.video_url,
    yr.sentiment_score,
    yr.key_insights,
    yr.published_at
FROM youtube_reviews yr
JOIN phones p ON yr.phone_id = p.id
WHERE yr.sentiment_score >= 50
ORDER BY yr.published_at DESC
LIMIT 50;


-- ============================================
-- Helper Functions
-- ============================================

-- Function: Get reviews for a phone
CREATE OR REPLACE FUNCTION get_phone_reviews(p_phone_id BIGINT)
RETURNS TABLE (
    channel_name VARCHAR,
    video_title TEXT,
    video_url TEXT,
    sentiment_score INTEGER,
    positive_points TEXT[],
    negative_points TEXT[],
    key_insights TEXT,
    published_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        yr.channel_name,
        yr.video_title,
        yr.video_url,
        yr.sentiment_score,
        yr.positive_points,
        yr.negative_points,
        yr.key_insights,
        yr.published_at
    FROM youtube_reviews yr
    WHERE yr.phone_id = p_phone_id
    ORDER BY yr.published_at DESC;
END;
$$ LANGUAGE plpgsql;


-- Function: Get average sentiment for a phone
CREATE OR REPLACE FUNCTION get_phone_sentiment(p_phone_id BIGINT)
RETURNS INTEGER AS $$
DECLARE
    avg_sentiment INTEGER;
BEGIN
    SELECT ROUND(AVG(sentiment_score))
    INTO avg_sentiment
    FROM youtube_reviews
    WHERE phone_id = p_phone_id;
    
    RETURN COALESCE(avg_sentiment, 0);
END;
$$ LANGUAGE plpgsql;


-- ============================================
-- Sample Queries (for testing)
-- ============================================

-- Get all reviews for a specific phone
-- SELECT * FROM get_phone_reviews(1);

-- Get review summary for all phones
-- SELECT * FROM phone_review_summary WHERE review_count > 0;

-- Get recent positive reviews
-- SELECT * FROM recent_positive_reviews LIMIT 10;

-- Find phones with mostly positive reviews
-- SELECT * FROM phone_review_summary 
-- WHERE avg_sentiment >= 50 AND review_count >= 3
-- ORDER BY avg_sentiment DESC;
