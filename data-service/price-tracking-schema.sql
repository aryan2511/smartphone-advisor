-- ============================================
-- Phase 2: Price Tracking System
-- Add dynamic price tracking with history
-- ============================================

-- Table: phone_prices
-- Stores current and historical prices from different retailers
CREATE TABLE IF NOT EXISTS phone_prices (
    id BIGSERIAL PRIMARY KEY,
    phone_id BIGINT NOT NULL REFERENCES phones(id) ON DELETE CASCADE,
    
    -- Retailer information
    source VARCHAR(20) NOT NULL, -- 'amazon', 'flipkart', 'official'
    
    -- Price data
    price INTEGER NOT NULL, -- Current price in rupees
    original_price INTEGER, -- MRP/Original price (for discount calculation)
    currency VARCHAR(3) DEFAULT 'INR',
    
    -- Product availability
    availability VARCHAR(20) DEFAULT 'in_stock', -- 'in_stock', 'out_of_stock', 'limited'
    
    -- URLs
    product_url TEXT, -- Direct product page
    asin VARCHAR(20), -- Amazon Standard Identification Number (for Amazon)
    
    -- Additional info from Amazon
    rating DECIMAL(2,1), -- Product rating (4.5/5.0)
    review_count INTEGER, -- Number of reviews
    
    -- Timestamps
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT unique_phone_source UNIQUE(phone_id, source)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_phone_prices_phone_id ON phone_prices(phone_id);
CREATE INDEX IF NOT EXISTS idx_phone_prices_source ON phone_prices(source);
CREATE INDEX IF NOT EXISTS idx_phone_prices_last_checked ON phone_prices(last_checked DESC);


-- Table: phone_price_history
-- Track price changes over time
CREATE TABLE IF NOT EXISTS phone_price_history (
    id BIGSERIAL PRIMARY KEY,
    phone_id BIGINT NOT NULL REFERENCES phones(id) ON DELETE CASCADE,
    source VARCHAR(20) NOT NULL,
    
    -- Price snapshot
    price INTEGER NOT NULL,
    original_price INTEGER,
    
    -- When this price was recorded
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Optional: Track why price changed
    change_reason VARCHAR(50) -- 'sale', 'discount', 'price_drop', 'price_increase'
);

-- Index for historical queries
CREATE INDEX IF NOT EXISTS idx_price_history_phone_source ON phone_price_history(phone_id, source);
CREATE INDEX IF NOT EXISTS idx_price_history_recorded_at ON phone_price_history(recorded_at DESC);


-- Table: amazon_products (optional - for caching Amazon data)
CREATE TABLE IF NOT EXISTS amazon_products (
    id BIGSERIAL PRIMARY KEY,
    phone_id BIGINT UNIQUE REFERENCES phones(id) ON DELETE CASCADE,
    
    -- Amazon identifiers
    asin VARCHAR(20) UNIQUE NOT NULL,
    
    -- Product metadata
    title TEXT,
    brand VARCHAR(100),
    model VARCHAR(100),
    
    -- Images
    main_image_url TEXT,
    additional_images JSONB, -- Array of image URLs
    
    -- Product details
    features JSONB, -- Key features as array
    specifications JSONB, -- Detailed specs as key-value
    
    -- SEO
    keywords TEXT[], -- Search keywords
    
    -- Sync metadata
    last_synced TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_amazon_products_asin ON amazon_products(asin);


-- ============================================
-- Views for Easy Querying
-- ============================================

-- View: Latest phone prices from all sources
CREATE OR REPLACE VIEW phone_current_prices AS
SELECT 
    p.id as phone_id,
    p.brand,
    p.model,
    pp.source,
    pp.price,
    pp.original_price,
    ROUND(((pp.original_price - pp.price)::DECIMAL / pp.original_price * 100), 0) as discount_percent,
    pp.availability,
    pp.product_url,
    pp.rating,
    pp.review_count,
    pp.last_checked
FROM phones p
LEFT JOIN phone_prices pp ON p.id = pp.phone_id
ORDER BY p.brand, p.model, pp.source;


-- View: Best price for each phone
CREATE OR REPLACE VIEW phone_best_prices AS
SELECT DISTINCT ON (phone_id)
    p.id as phone_id,
    p.brand,
    p.model,
    pp.source as best_price_source,
    pp.price as best_price,
    pp.product_url,
    pp.availability
FROM phones p
LEFT JOIN phone_prices pp ON p.id = pp.phone_id
WHERE pp.availability = 'in_stock'
ORDER BY phone_id, pp.price ASC;


-- View: Price history with changes
CREATE OR REPLACE VIEW phone_price_trends AS
SELECT 
    ph.phone_id,
    p.brand || ' ' || p.model as phone_name,
    ph.source,
    ph.price,
    ph.recorded_at,
    LAG(ph.price) OVER (PARTITION BY ph.phone_id, ph.source ORDER BY ph.recorded_at) as previous_price,
    ph.price - LAG(ph.price) OVER (PARTITION BY ph.phone_id, ph.source ORDER BY ph.recorded_at) as price_change
FROM phone_price_history ph
JOIN phones p ON ph.phone_id = p.id
ORDER BY ph.recorded_at DESC;


-- ============================================
-- Helper Functions
-- ============================================

-- Function: Update price and record history
CREATE OR REPLACE FUNCTION update_phone_price(
    p_phone_id BIGINT,
    p_source VARCHAR,
    p_new_price INTEGER,
    p_original_price INTEGER,
    p_availability VARCHAR,
    p_product_url TEXT,
    p_rating DECIMAL,
    p_review_count INTEGER
)
RETURNS VOID AS $$
DECLARE
    v_old_price INTEGER;
BEGIN
    -- Get old price
    SELECT price INTO v_old_price 
    FROM phone_prices 
    WHERE phone_id = p_phone_id AND source = p_source;
    
    -- Upsert current price
    INSERT INTO phone_prices (
        phone_id, source, price, original_price, 
        availability, product_url, rating, review_count, last_checked
    )
    VALUES (
        p_phone_id, p_source, p_new_price, p_original_price,
        p_availability, p_product_url, p_rating, p_review_count, CURRENT_TIMESTAMP
    )
    ON CONFLICT (phone_id, source)
    DO UPDATE SET
        price = p_new_price,
        original_price = p_original_price,
        availability = p_availability,
        product_url = p_product_url,
        rating = p_rating,
        review_count = p_review_count,
        last_checked = CURRENT_TIMESTAMP;
    
    -- Record in history if price changed
    IF v_old_price IS NULL OR v_old_price != p_new_price THEN
        INSERT INTO phone_price_history (phone_id, source, price, original_price)
        VALUES (p_phone_id, p_source, p_new_price, p_original_price);
    END IF;
END;
$$ LANGUAGE plpgsql;


-- ============================================
-- Sample Queries (for testing)
-- ============================================

-- Get phone with all current prices
-- SELECT * FROM phone_current_prices WHERE phone_id = 1;

-- Get best available price for a phone
-- SELECT * FROM phone_best_prices WHERE phone_id = 1;

-- Get price history for a phone
-- SELECT * FROM phone_price_trends WHERE phone_id = 1 AND source = 'amazon';

-- Get all phones with prices in a budget range
-- SELECT DISTINCT phone_id, brand, model, best_price 
-- FROM phone_best_prices 
-- WHERE best_price BETWEEN 60000 AND 75000;
