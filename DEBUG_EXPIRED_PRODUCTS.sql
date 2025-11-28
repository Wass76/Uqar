-- استعلامات للتحقق من المنتجات المنتهية الصلاحية
-- استخدم هذه الاستعلامات للتحقق من البيانات الفعلية في Database

-- 1. التحقق من StockItems للمنتج 153 في الصيدلية 52
SELECT 
    si.id,
    si.product_id,
    si.product_type,
    si.expiry_date,
    si.quantity,
    si.pharmacy_id,
    CASE 
        WHEN si.expiry_date < CURRENT_DATE THEN 'منتهي'
        WHEN si.expiry_date = CURRENT_DATE THEN 'ينتهي اليوم'
        WHEN si.expiry_date <= CURRENT_DATE + INTERVAL '30 days' THEN 'قريب من الانتهاء'
        ELSE 'طبيعي'
    END as status,
    CURRENT_DATE - si.expiry_date as days_since_expiry,
    si.expiry_date - CURRENT_DATE as days_until_expiry
FROM stock_item si
WHERE si.product_id = 153 
  AND si.pharmacy_id = 52
ORDER BY si.expiry_date ASC;

-- 2. جميع StockItems المنتهية الصلاحية في الصيدلية 52
SELECT 
    si.id,
    si.product_id,
    si.product_type,
    si.expiry_date,
    si.quantity,
    si.pharmacy_id,
    CURRENT_DATE - si.expiry_date as days_since_expiry
FROM stock_item si
WHERE si.pharmacy_id = 52
  AND si.expiry_date < CURRENT_DATE
  AND si.quantity > 0
ORDER BY si.expiry_date ASC;

-- 3. جميع StockItems القريبة من الانتهاء (خلال 30 يوم) في الصيدلية 52
SELECT 
    si.id,
    si.product_id,
    si.product_type,
    si.expiry_date,
    si.quantity,
    si.pharmacy_id,
    si.expiry_date - CURRENT_DATE as days_until_expiry
FROM stock_item si
WHERE si.pharmacy_id = 52
  AND si.expiry_date > CURRENT_DATE
  AND si.expiry_date <= CURRENT_DATE + INTERVAL '30 days'
  AND si.quantity > 0
ORDER BY si.expiry_date ASC;

-- 4. التحقق من الموظفين في الصيدلية 52
SELECT 
    e.id,
    e.name,
    e.role_id,
    r.name as role_name,
    e.pharmacy_id
FROM employee e
LEFT JOIN role r ON e.role_id = r.id
WHERE e.pharmacy_id = 52;

-- 5. التحقق من حالة الصيدلية 52
SELECT 
    id,
    name,
    is_active,
    type
FROM pharmacy
WHERE id = 52;

