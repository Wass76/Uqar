package com.Uqar.moneybox.enums;

public enum TransactionType {
    OPENING_BALANCE,
    CASH_DEPOSIT,
    CASH_WITHDRAWAL,
    SALE_PAYMENT,
    SALE_REFUND,
    PURCHASE_PAYMENT,
    PURCHASE_REFUND,
    DEBT_PAYMENT, // ✅ ADDED: For customer debt payments
    EXPENSE,
    INCOME,
    TRANSFER_IN,
    TRANSFER_OUT,
    ADJUSTMENT,
    CLOSING_BALANCE
}
