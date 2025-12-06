-- Migration: Add reason and notes columns to stock_item table
-- Description: Adds reason and notes columns for inventory adjustments without purchase invoice
-- Author: System
-- Date: 2025-01-17

-- Add reason column to stock_item table
ALTER TABLE stock_item 
ADD COLUMN IF NOT EXISTS reason VARCHAR(1000);

-- Add notes column to stock_item table
ALTER TABLE stock_item 
ADD COLUMN IF NOT EXISTS notes VARCHAR(2000);

-- Add comments
COMMENT ON COLUMN stock_item.reason IS 'سبب الإضافة (للتعديلات بدون فاتورة) - Reason for adding stock (for adjustments without invoice)';
COMMENT ON COLUMN stock_item.notes IS 'ملاحظات إضافية - Additional notes';
