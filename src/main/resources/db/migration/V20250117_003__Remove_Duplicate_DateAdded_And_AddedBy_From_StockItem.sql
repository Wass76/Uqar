-- Migration: Remove duplicate dateAdded and addedBy columns from stock_item table
-- Description: These fields are redundant as they are already provided by AuditedEntity (createdAt, createdBy)
-- Author: System
-- Date: 2025-01-17

-- Before dropping, migrate any data from dateAdded/addedBy to createdAt/createdBy if needed
-- Note: createdAt and createdBy are automatically populated by AuditingEntityListener,
-- but we'll preserve any existing data in dateAdded/addedBy if createdAt/createdBy are null

DO $$
BEGIN
    -- Update createdAt from dateAdded if createdAt is null but dateAdded exists
    UPDATE stock_item 
    SET created_at = date_added::timestamp 
    WHERE created_at IS NULL 
      AND date_added IS NOT NULL;
    
    -- Update createdBy from addedBy if createdBy is null but addedBy exists
    UPDATE stock_item 
    SET created_by = added_by 
    WHERE created_by IS NULL 
      AND added_by IS NOT NULL;
END $$;

-- Drop the duplicate columns
ALTER TABLE stock_item DROP COLUMN IF EXISTS date_added;
ALTER TABLE stock_item DROP COLUMN IF EXISTS added_by;

-- Add comment to document the change
COMMENT ON COLUMN stock_item.created_at IS 'تاريخ الإنشاء - يتم تعيينه تلقائياً من AuditedEntity (replaces dateAdded)';
COMMENT ON COLUMN stock_item.created_by IS 'المستخدم الذي أنشأ السجل - يتم تعيينه تلقائياً من AuditedEntity (replaces addedBy)';

