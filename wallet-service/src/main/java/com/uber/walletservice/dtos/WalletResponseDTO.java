package com.uber.walletservice.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WalletResponseDTO {
    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private LocalDateTime lastUpdated;
    private List<TransactionDTO> recentTransactions;
}