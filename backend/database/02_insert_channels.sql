-- Insert credible YouTube channels for phone reviews
-- These channels are used for sentiment analysis

INSERT INTO youtube_channels (channel_id, channel_name, subscriber_count, credibility_score, tier, language, focus_areas, verified, active) VALUES
('UCRiDxLhkhkfJK0KLcVuWK-Q', 'Mrwhosetheboss', 19500000, 95, 1, 'EN', '["smartphones", "tech_reviews", "comparisons"]', true, true),
('UCBJycsmduvYEL83R_U4JriQ', 'MKBHD', 19200000, 98, 1, 'EN', '["smartphones", "tech_reviews", "flagship_devices"]', true, true),
('UC3SEvBYhullC-aaETV4k2Mw', 'Trakin Tech', 8900000, 85, 2, 'EN', '["smartphones", "indian_market", "budget_phones"]', true, true),
('UC8vXJhMLjML0NVcujmjq64Q', 'Geeky Ranjit', 3100000, 82, 2, 'EN', '["smartphones", "tech_reviews", "indian_market"]', true, true),
('UCL4dFdrYeZnnPJx0JcdzMHw', 'C4ETech', 3800000, 80, 2, 'EN', '["smartphones", "detailed_reviews", "indian_market"]', true, true),
('UCpp6iYPrWHTIYQmrcCMyNhA', 'Beebom', 4200000, 78, 2, 'EN', '["smartphones", "tech_news", "how_to"]', true, true),
('UC-vHsnjFYm6b3OH3Su6KLEA', 'TechBar', 1200000, 75, 3, 'EN', '["smartphones", "reviews", "tech_tips"]', true, true),
('UCwVYGZMCy1pPi7aKSDK7Atw', 'TechWiser', 2100000, 76, 3, 'EN', '["smartphones", "tech_reviews", "comparisons"]', true, true)
ON CONFLICT (channel_id) DO NOTHING;
