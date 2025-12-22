-- Migration: Add partial selling feature fields
-- Description: Adds fields for partial selling feature (selling parts of a box instead of full boxes)
-- Author: System
-- Date: 2025-12-21

-- Add number_of_parts_per_box to master_product table
ALTER TABLE master_product 
ADD COLUMN IF NOT EXISTS number_of_parts_per_box INTEGER;

-- Add comment for master_product.number_of_parts_per_box
COMMENT ON COLUMN master_product.number_of_parts_per_box IS 
'عدد الأجزاء في العلبة (للبيع الجزئي) - Number of parts per box (for partial selling). null أو 0 أو 1 = لا يباع جزئياً, > 1 = يمكن بيع جزء من العلبة';

-- Add number_of_parts_per_box to pharmacy_product table
ALTER TABLE pharmacy_product 
ADD COLUMN IF NOT EXISTS number_of_parts_per_box INTEGER;

-- Add comment for pharmacy_product.number_of_parts_per_box
COMMENT ON COLUMN pharmacy_product.number_of_parts_per_box IS 
'عدد الأجزاء في العلبة (للبيع الجزئي) - Number of parts per box (for partial selling). null أو 0 أو 1 = لا يباع جزئياً, > 1 = يمكن بيع جزء من العلبة';

-- Add remaining_parts to stock_item table
ALTER TABLE stock_item 
ADD COLUMN IF NOT EXISTS remaining_parts INTEGER;

-- Add comment for stock_item.remaining_parts
COMMENT ON COLUMN stock_item.remaining_parts IS 
'عدد الأجزاء المتبقية من العلبة الحالية (للبيع الجزئي) - Remaining parts from the current box (for partial selling). null = المنتج لا يباع جزئياً أو العلبة لم تُفتح بعد, > 0 = عدد الأجزاء المتبقية, عندما يصبح 0: يتم خصم علبة كاملة من quantity';

-- Add parts_sold to sale_invoice_items table
ALTER TABLE sale_invoice_items 
ADD COLUMN IF NOT EXISTS parts_sold INTEGER;

-- Add comment for sale_invoice_items.parts_sold
COMMENT ON COLUMN sale_invoice_items.parts_sold IS 
'عدد الأجزاء المباعة (للبيع الجزئي) - Number of parts sold (for partial selling). null = بيع علبة كاملة, > 0 = عدد الأجزاء المباعة من العلبة';

