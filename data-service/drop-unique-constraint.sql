-- Drop the unique constraint if it exists
-- Run this in your Neon database console

-- First, check what constraints exist
SELECT constraint_name 
FROM information_schema.table_constraints 
WHERE table_name = 'phones' AND constraint_type = 'UNIQUE';

-- Drop the constraint (replace 'constraint_name_here' with the actual name from above)
-- ALTER TABLE phones DROP CONSTRAINT unique_brand_model;
-- ALTER TABLE phones DROP CONSTRAINT phones_brand_model_key;

-- Or drop all unique constraints except primary key:
DO $$ 
DECLARE 
    constraint_record RECORD;
BEGIN
    FOR constraint_record IN 
        SELECT constraint_name 
        FROM information_schema.table_constraints 
        WHERE table_name = 'phones' 
        AND constraint_type = 'UNIQUE'
    LOOP
        EXECUTE 'ALTER TABLE phones DROP CONSTRAINT ' || constraint_record.constraint_name;
        RAISE NOTICE 'Dropped constraint: %', constraint_record.constraint_name;
    END LOOP;
END $$;
