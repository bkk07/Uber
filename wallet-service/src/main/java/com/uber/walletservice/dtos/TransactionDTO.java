package com.uber.walletservice.dtos;

import com.uber.walletservice.enums.TransactionFor;
import com.uber.walletservice.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private Long transactionId;
    private TransactionType type;
    private TransactionFor transactionFor;
    private BigDecimal amount;
    private Long referenceId;
    private LocalDateTime createdAt;
}