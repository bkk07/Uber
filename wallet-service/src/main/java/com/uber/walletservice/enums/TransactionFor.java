package com.uber.walletservice.enums;

public enum TransactionFor {
    TOP_UP,
    CASHBACK,
    RIDE_PAYMENT,
    REFUND,
    PROMO // When promo code itself adds balance directly
}