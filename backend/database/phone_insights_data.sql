-- ============================================
-- Manual Phone Insights for All 8 Phones
-- Run this in your Neon SQL Editor
-- ============================================

-- Samsung Galaxy S24 (ID: 1)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    1,
    'camera-performance-battery',
    'You wanted a phone that takes amazing photos first, then handles everything smoothly. The Samsung S24 nails both. Its camera captures sharp, vibrant photos even when it''s dark. Apps and games run without a hiccup.',
    E'• Your Instagram will look professional\n• Night shots that actually look good\n• Apps open instantly, no waiting\n• Battery gets you through the day\n• People will compliment your photos',
    'The battery is good but not the longest-lasting. If you''re constantly on your phone all day, you might need a quick charge in the evening. But for most people, it''s plenty.',
    3,
    'Better zoom capabilities and ₹15,000 cheaper than iPhone 15',
    true
);

-- OnePlus 12 (ID: 2)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    2,
    'battery-performance-camera',
    'You need a phone that lasts all day without dying on you. The OnePlus 12 has incredible battery life - easily gets you through a full day of heavy use. Plus it''s super fast and takes really good photos.',
    E'• Forget about battery anxiety\n• Charge once every 1-2 days\n• Blazing fast performance\n• Great camera that doesn''t drain battery\n• Won''t leave you stranded',
    'The camera is very good, but if you''re a photography enthusiast who loves night shots, the Samsung S24 has a slight edge there. For most people though, this camera is more than enough.',
    1,
    'Better battery life (5400 mAh) and ₹15,000 cheaper',
    true
);

-- iPhone 15 (ID: 3)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    3,
    'privacy-performance-camera',
    'You care about keeping your data private. iPhone 15 is the gold standard for privacy and security. Your personal info stays yours. Plus you get flagship performance and an excellent camera system.',
    E'• Best-in-class privacy protections\n• Regular security updates for years\n• Smooth iOS experience\n• Great camera, especially for video\n• Works perfectly with other Apple devices',
    'It''s expensive at ₹79,900 and the base model only has 128GB storage. Also, if you want to really customize your phone, Android gives you more freedom.',
    1,
    'Industry-leading privacy features and better app ecosystem control',
    true
);

-- Xiaomi 14 Pro (ID: 4)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    4,
    'camera-design-performance',
    'You wanted flagship camera quality with stunning design. The Xiaomi 14 Pro delivers both with its Leica partnership and premium build. Great performance too with the latest Snapdragon chip.',
    E'• Leica-tuned camera takes gorgeous photos\n• Premium materials and design\n• Fast performance for anything you throw at it\n• Good value at ₹69,999\n• Large, beautiful display',
    'Some users report heating during extended camera use or gaming. Also, MIUI (their Android skin) has ads and bloatware that can be annoying. Battery life is decent but not exceptional.',
    1,
    'Leica camera partnership and unique design at lower price',
    true
);

-- Google Pixel 8 Pro (ID: 5)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    5,
    'camera-privacy-software',
    'You want the smartest camera that almost thinks for itself. Pixel 8 Pro has unmatched computational photography - photos look amazing with minimal effort. Clean Android experience and strong privacy features too.',
    E'• Best point-and-shoot camera\n• Magic Eraser and AI photo editing built-in\n• Cleanest Android experience\n• 7 years of software updates guaranteed\n• Excellent call screening and spam protection',
    'At ₹1,06,999, it''s very expensive. Performance is good but not the absolute fastest. Battery life is okay but heavy users might want more. Gets warm during intensive tasks.',
    1,
    'Unmatched AI camera features and longest software support',
    true
);

-- Realme GT 5 Pro (ID: 6)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    6,
    'performance-battery-camera',
    'You want flagship performance without the flagship price. At ₹42,999, the Realme GT 5 Pro gives you a top-tier chip, huge battery, and surprisingly good cameras. It''s incredible value.',
    E'• Flagship performance at mid-range price\n• Massive 5400 mAh battery\n• Great for gaming\n• Fast charging\n• Solid camera for the price',
    'Build quality feels less premium than pricier phones. The UI (Realme UI) has some bloatware. Camera is good but not exceptional - fine for social media, not for photography enthusiasts. Brand perception isn''t premium.',
    2,
    'Same performance and battery at ₹22,000 less',
    true
);

-- Nothing Phone (2a) (ID: 7)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    7,
    'design-camera-software',
    'You want something different that stands out. Nothing Phone (2a) has unique glyph lighting on the back, clean software, and surprisingly good cameras for just ₹23,999. Best design in this price range.',
    E'• Unique transparent design with LED glyphs\n• Clean, near-stock Android\n• Great camera for the price\n• Stands out from boring phones\n• Regular software updates',
    'It''s a mid-range phone, so performance is decent but not flagship-level. Not ideal for heavy gaming. Battery is good but not exceptional. Limited availability - can sell out quickly.',
    6,
    'Unique design and cleaner software, though less powerful',
    true
);

-- Vivo X100 Pro (ID: 8)
INSERT INTO phone_insights (phone_id, priority_pattern, why_picked, why_love_it, what_to_know, beats_alternative_id, beats_reason, is_manual)
VALUES (
    8,
    'camera-performance-design',
    'You''re a serious photography enthusiast who wants the absolute best camera. The Vivo X100 Pro, co-engineered with Zeiss, has one of the best smartphone camera systems available. Flagship performance and premium build too.',
    E'• Zeiss-tuned cameras with incredible detail\n• Excellent telephoto and portrait shots\n• Premium design and build\n• Powerful performance\n• Great display',
    'At ₹89,999, it''s expensive. FunTouch OS (Vivo''s Android skin) is not as clean as stock Android. Brand awareness is lower than Samsung/Apple. Some features are gimmicky.',
    1,
    'Superior telephoto camera and Zeiss partnership for serious photography',
    true
);

-- Verification: Check what was inserted
SELECT phone_id, priority_pattern, LEFT(why_picked, 60) as preview
FROM phone_insights
ORDER BY phone_id;
