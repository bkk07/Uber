package com.uber.walletservice.enums;

public enum DiscountType {
    CASHBACK, // Adds money to wallet
    DISCOUNT // Reduces amount of payment (handled by payment service, not wallet balance directly in this design)
}