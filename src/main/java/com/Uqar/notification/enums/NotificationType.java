    package com.Uqar.notification.enums;

public enum NotificationType {
    // تنبيهات المخزون
    STOCK_LOW,              // انخفاض المخزون
    STOCK_EXPIRED,          // منتج منتهي الصلاحية
    STOCK_EXPIRING_SOON,    // منتج قريب منتهي الصلاحية
    
    // تنبيهات مالية
    DEBT_CREATED,           // دين جديد
    DEBT_OVERDUE,           // دين متأخر
    DEBT_PAID,              // دين تم سداده
    PURCHASE_LIMIT_EXCEEDED, // تجاوز حد مالي في الشراء
    
    // تنبيهات المبيعات
    SALE_CREATED,           // فاتورة بيع جديدة
    SALE_REFUNDED,          // مرتجع
    
    // تنبيهات المشتريات
    PURCHASE_ORDER_CREATED, // طلب شراء جديد
    PURCHASE_INVOICE_RECEIVED // وصول فاتورة شراء
}

