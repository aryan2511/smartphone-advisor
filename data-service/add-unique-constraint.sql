-- Add unique constraint to phones table
-- This allows us to use ON CONFLICT in INSERT statements

-- First, check if there are any duplicate brand+model combinations
SELECT brand, model, COUNT(*) as count
FROM phones 
GROUP BY brand, model 
HAVING COUNT(*) > 1;

-- If there are duplicates, you might want to clean them up first
-- DELETE FROM phones WHERE id IN (
--   SELECT id FROM (
--     SELECT id, ROW_NUMBER() OVER (PARTITION BY brand, model ORDER BY id) as row_num
--     FROM phones
--   ) t WHERE t.row_num > 1
-- );

-- Now add the unique constraint
ALTER TABLE phones 
ADD CONSTRAINT unique_brand_model UNIQUE (brand, model);

-- Verify the constraint was added
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'phones' AND constraint_type = 'UNIQUE';
