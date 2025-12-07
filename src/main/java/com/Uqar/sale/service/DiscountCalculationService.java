package com.Uqar.sale.service;

import com.Uqar.product.Enum.DiscountType;
import com.Uqar.sale.dto.DiscountDTO;
import org.springframework.stereotype.Service;

@Service
public class DiscountCalculationService {
    
    /**
     * حساب قيمة الخصم
     * @param originalAmount المبلغ الأصلي
     * @param discountType نوع الخصم
     * @param discountValue قيمة الخصم
     * @return قيمة الخصم المحسوبة
     */
    public Float calculateDiscount(Float originalAmount, DiscountType discountType, Float discountValue) {
        if (originalAmount == null || originalAmount <= 0 || discountType == null || discountValue == null || discountValue <= 0) {
            return 0f;
        }
        
        switch (discountType) {
            case PERCENTAGE:
                return (originalAmount * discountValue) / 100f;
            case FIXED_AMOUNT:
                return Math.min(discountValue, originalAmount);
            default:
                return 0f;
        }
    }
    
    /**
     * الحصول على المبلغ بعد الخصم
     * @param originalAmount المبلغ الأصلي
     * @param discountType نوع الخصم
     * @param discountValue قيمة الخصم
     * @return المبلغ بعد الخصم
     */
    public Float getAmountAfterDiscount(Float originalAmount, DiscountType discountType, Float discountValue) {
        Float discount = calculateDiscount(originalAmount, discountType, discountValue);
        return originalAmount - discount;
    }
    
    /**
     * حساب الخصم باستخدام DiscountDTO
     * @param originalAmount المبلغ الأصلي
     * @param discountDTO كائن الخصم
     * @return قيمة الخصم المحسوبة
     */
    public Float calculateDiscount(Float originalAmount, DiscountDTO discountDTO) {
        if (discountDTO == null) {
            return 0f;
        }
        return calculateDiscount(originalAmount, discountDTO.getType(), discountDTO.getValue());
    }
    
    /**
     * الحصول على المبلغ بعد الخصم باستخدام DiscountDTO
     * @param originalAmount المبلغ الأصلي
     * @param discountDTO كائن الخصم
     * @return المبلغ بعد الخصم
     */
    public Float getAmountAfterDiscount(Float originalAmount, DiscountDTO discountDTO) {
        if (discountDTO == null) {
            return originalAmount;
        }
        return getAmountAfterDiscount(originalAmount, discountDTO.getType(), discountDTO.getValue());
    }
} 