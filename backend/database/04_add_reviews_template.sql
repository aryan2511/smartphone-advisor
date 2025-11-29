-- QUICK START: Add YouTube Reviews to Database
-- This script helps you quickly add video reviews for phones

-- Step 1: Get Phone IDs
SELECT id, brand, model, price 
FROM phones 
ORDER BY price 
LIMIT 20;

-- Step 2: Get Channel IDs
SELECT id, channel_name, credibility_score
FROM youtube_channels
ORDER BY credibility_score DESC;

-- Step 3: Add Reviews (Template)
-- Replace values with actual data from YouTube

-- TEMPLATE:
-- INSERT INTO phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
-- VALUES (PHONE_ID, (SELECT id FROM youtube_channels WHERE channel_name = 'CHANNEL'), 'VIDEO_ID', 'TITLE', 'URL');

-- EXAMPLE: Samsung Galaxy S24
-- Find video: https://www.youtube.com/watch?v=Zi8vJ_lMxQI
-- Video ID is: Zi8vJ_lMxQI

INSERT INTO phone_reviews (phone_id, channel_id, video_id, video_title, video_url)
VALUES 
(1, (SELECT id FROM youtube_channels WHERE channel_name = 'MKBHD'), 'Zi8vJ_lMxQI', 'Samsung Galaxy S24 Ultra Review', 'https://www.youtube.com/watch?v=Zi8vJ_lMxQI'),
(1, (SELECT id FROM youtube_channels WHERE channel_name = 'Mrwhosetheboss'), 'ABC123', 'S24 Ultra - Best Phone?', 'https://www.youtube.com/watch?v=ABC123');

-- Add more phones here...

-- Step 4: Verify
SELECT 
    p.brand,
    p.model,
    c.channel_name,
    r.video_title,
    r.video_url,
    CASE 
        WHEN r.transcript IS NULL THEN 'Pending'
        WHEN r.sentiment_score IS NULL THEN 'Transcript Ready'
        ELSE 'Complete'
    END as status
FROM phone_reviews r
JOIN phones p ON r.phone_id = p.id
JOIN youtube_channels c ON r.channel_id = c.id
ORDER BY p.id, c.credibility_score DESC;

-- Step 5: Count coverage
SELECT 
    COUNT(DISTINCT phone_id) as phones_with_reviews,
    COUNT(*) as total_reviews,
    AVG(reviews_per_phone) as avg_reviews_per_phone
FROM (
    SELECT phone_id, COUNT(*) as reviews_per_phone
    FROM phone_reviews
    GROUP BY phone_id
) subquery;
