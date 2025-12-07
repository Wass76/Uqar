package com.Uqar.sale.service;

import com.Uqar.product.Enum.PaymentType;
import com.Uqar.product.Enum.PaymentMethod;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidationService {
 
    public boolean validatePayment(PaymentType paymentType, PaymentMethod paymentMethod) {
        if (paymentType == null || paymentMethod == null) {
            return false;
        }
        
        switch (paymentType) {
            case CASH:
                return paymentMethod == PaymentMethod.CASH || 
                       paymentMethod == PaymentMethod.BANK_ACCOUNT;
                       
            case CREDIT:
                return paymentMethod == PaymentMethod.CASH ||
                       paymentMethod == PaymentMethod.BANK_ACCOUNT;
                       
            default:
                return false;
        }
    }
    

    public boolean validatePaidAmount(float totalAmount, float paidAmount, PaymentType paymentType) {
        switch (paymentType) {
            case CASH:
                return paidAmount >= 0 && paidAmount >= totalAmount;
                
            case CREDIT:    
                return paidAmount >= 0;
                
            default:
                return false;
        }
    }
    
 
    public float calculateRemainingAmount(float totalAmount, float paidAmount) {
        return Math.max(0, totalAmount - paidAmount);
    }
    
    
    public boolean isPaymentComplete(float totalAmount, float paidAmount) {
        return paidAmount >= totalAmount;
    }
} 