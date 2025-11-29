-- Update phones table to handle longer spec text from GSMArena
-- Run this in your Neon SQL Editor

-- Increase size limits for spec columns
ALTER TABLE phones ALTER COLUMN display TYPE TEXT;
ALTER TABLE phones ALTER COLUMN processor TYPE TEXT;
ALTER TABLE phones ALTER COLUMN ram TYPE VARCHAR(50);
ALTER TABLE phones ALTER COLUMN storage TYPE VARCHAR(50);
ALTER TABLE phones ALTER COLUMN battery TYPE VARCHAR(50);
ALTER TABLE phones ALTER COLUMN camera TYPE TEXT;

-- Verify changes
SELECT 
    column_name, 
    data_type, 
    character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'phones' 
AND column_name IN ('display', 'processor', 'ram', 'storage', 'battery', 'camera')
ORDER BY column_name;
