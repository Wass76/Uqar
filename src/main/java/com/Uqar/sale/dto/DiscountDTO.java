package com.Uqar.sale.dto;

import com.Uqar.product.Enum.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiscountDTO {
    private DiscountType type;
    private Float value; // النسبة المئوية أو المبلغ الثابت
    
    /**
     * حساب قيمة الخصم
     * @param originalAmount المبلغ الأصلي
     * @return قيمة الخصم
     */
    public Float calculateDiscount(Float originalAmount) {
        if (value == null || value <= 0) {
            return 0f;
        }
        
        switch (type) {
            case PERCENTAGE:
                return (originalAmount * value) / 100f;
            case FIXED_AMOUNT:
                return Math.min(value, originalAmount); // لا يزيد الخصم عن المبلغ الأصلي
            default:
                return 0f;
        }
    }
    
    /**
     * الحصول على المبلغ بعد الخصم
     * @param originalAmount المبلغ الأصلي
     * @return المبلغ بعد الخصم
     */
    public Float getAmountAfterDiscount(Float originalAmount) {
        return originalAmount - calculateDiscount(originalAmount);
    }
} 