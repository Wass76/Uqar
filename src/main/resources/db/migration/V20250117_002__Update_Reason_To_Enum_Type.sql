-- Migration: Update reason column to use enum type
-- Description: Changes reason column from VARCHAR(1000) to VARCHAR(50) with CHECK constraint for enum values
-- Author: System
-- Date: 2025-01-17

-- First, update any existing invalid values to NULL (optional, for data cleanup)
-- You can customize this based on your needs
UPDATE stock_item 
SET reason = NULL 
WHERE reason IS NOT NULL 
  AND reason NOT IN ('INVENTORY_COUNT', 'PHYSICAL_COUNT_ADJUSTMENT');

-- Modify the existing column to use proper type and constraint
DO $$
BEGIN
    -- Check if column exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'stock_item' 
        AND column_name = 'reason'
    ) THEN
        -- Change column type to VARCHAR(50)
        ALTER TABLE stock_item 
        ALTER COLUMN reason TYPE VARCHAR(50) USING reason::VARCHAR(50);
    ELSE
        -- Add the new reason column if it doesn't exist
        ALTER TABLE stock_item 
        ADD COLUMN reason VARCHAR(50);
    END IF;
END $$;

-- Drop existing constraint if it exists
ALTER TABLE stock_item DROP CONSTRAINT IF EXISTS chk_stock_item_reason;

-- Add CHECK constraint to ensure only valid enum values
ALTER TABLE stock_item 
ADD CONSTRAINT chk_stock_item_reason 
CHECK (reason IS NULL OR reason IN (
    'INVENTORY_COUNT',
    'PHYSICAL_COUNT_ADJUSTMENT'
));

-- Add comment
COMMENT ON COLUMN stock_item.reason IS 'سبب الإضافة (للتعديلات بدون فاتورة) - Inventory adjustment reason enum value';

