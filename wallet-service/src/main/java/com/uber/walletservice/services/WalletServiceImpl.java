package com.uber.walletservice.services;

import com.uber.walletservice.dtos.*;
import com.uber.walletservice.enums.DiscountType;
import com.uber.walletservice.enums.TransactionFor;
import com.uber.walletservice.enums.TransactionType;
import com.uber.walletservice.exceptions.InsufficientBalanceException;
import com.uber.walletservice.exceptions.InvalidPromoException;
import com.uber.walletservice.exceptions.WalletNotFoundException;
import com.uber.walletservice.models.Promo;
import com.uber.walletservice.models.UserWallet;
import com.uber.walletservice.models.WalletTransaction;
import com.uber.walletservice.repositories.PromoRepository;
import com.uber.walletservice.repositories.UserWalletRepository;
import com.uber.walletservice.repositories.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // For logging
public class WalletServiceImpl implements WalletService {

    private final UserWalletRepository userWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PromoRepository promoRepository;
    private static final int RECENT_TRANSACTIONS_LIMIT = 10;

    @Override
    public WalletResponseDTO getWalletDetails(Long userId) {
        // Fetches UserWallet based on the userid from the userWalletRepository
        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + userId));
        log.info("Fetched wallet details for user ID: {}. Wallet ID: {}", userId, wallet.getWalletId());
        // It fetches the last 10 transactions from the wallet transaction repository
        List<WalletTransaction> transactions = walletTransactionRepository
                .findByUserWallet_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT));
        return buildWalletResponseDTO(wallet, transactions);
    }

    @Override
    @Transactional
    public WalletResponseDTO addMoney(AddMoneyRequestDTO request) {
        // Use lock for concurrent updates
        UserWallet wallet = userWalletRepository.findByUserIdWithLock(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + request.getUserId()));

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive.");
        }

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        UserWallet updatedWallet = userWalletRepository.save(wallet); // This updates `updatedAt` field due to @UpdateTimestamp

        WalletTransaction transaction = WalletTransaction.builder()
                .userWallet(updatedWallet)
                .type(TransactionType.CREDIT)
                .transactionFor(TransactionFor.TOP_UP)
                .amount(request.getAmount())
                .referenceId(request.getPaymentReferenceId())
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Money added to wallet {} for user {}. Amount: {}. New Balance: {}",
                wallet.getWalletId(), wallet.getUserId(), request.getAmount(), updatedWallet.getBalance());

        // Re-fetch transactions for the response or consider including it directly if recent trans not needed
        List<WalletTransaction> transactions = walletTransactionRepository
                .findByUserWallet_UserIdOrderByCreatedAtDesc(request.getUserId(), PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT));

        return buildWalletResponseDTO(updatedWallet, transactions);
    }

    @Override
    @Transactional
    public WalletResponseDTO deductMoney(DeductMoneyRequestDTO request) {
        // Use lock for concurrent updates
        UserWallet wallet = userWalletRepository.findByUserIdWithLock(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + request.getUserId()));

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to deduct must be positive.");
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient balance for user {}. Requested: {}, Available: {}",
                    request.getUserId(), request.getAmount(), wallet.getBalance());
            throw new InsufficientBalanceException("Insufficient balance. Available: " + wallet.getBalance() + ", Requested: " + request.getAmount());
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        UserWallet updatedWallet = userWalletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .userWallet(updatedWallet)
                .type(TransactionType.DEBIT)
                .transactionFor(TransactionFor.RIDE_PAYMENT)
                .amount(request.getAmount())
                .referenceId(request.getRideId())
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Money deducted from wallet {} for user {}. Amount: {}. New Balance: {}",
                wallet.getWalletId(), wallet.getUserId(), request.getAmount(), updatedWallet.getBalance());

        List<WalletTransaction> transactions = walletTransactionRepository
                .findByUserWallet_UserIdOrderByCreatedAtDesc(request.getUserId(), PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT));

        return buildWalletResponseDTO(updatedWallet, transactions);
    }

    @Override
    @Transactional
    public WalletResponseDTO applyPromo(ApplyPromoRequestDTO request) {
        UserWallet wallet = userWalletRepository.findByUserIdWithLock(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + request.getUserId()));

        Promo promo = promoRepository.findByCode(request.getPromoCode())
                .orElseThrow(() -> new InvalidPromoException("Promo code not found: " + request.getPromoCode()));

        if (promo.getValidTill().isBefore(LocalDate.now())) {
            throw new InvalidPromoException("Promo code has expired: " + request.getPromoCode());
        }

        if (promo.getDiscountType() == DiscountType.CASHBACK) {
            wallet.setBalance(wallet.getBalance().add(promo.getAmount()));
            UserWallet updatedWallet = userWalletRepository.save(wallet);

            WalletTransaction transaction = WalletTransaction.builder()
                    .userWallet(updatedWallet)
                    .type(TransactionType.CREDIT)
                    .transactionFor(TransactionFor.PROMO)
                    .amount(promo.getAmount())
                    .referenceId(promo.getPromoId())
                    .build();
            walletTransactionRepository.save(transaction);

            log.info("Cashback applied to wallet {} for user {}. Promo: {}. Amount: {}. New Balance: {}",
                    wallet.getWalletId(), wallet.getUserId(), request.getPromoCode(), promo.getAmount(), updatedWallet.getBalance());

            List<WalletTransaction> transactions = walletTransactionRepository
                    .findByUserWallet_UserIdOrderByCreatedAtDesc(request.getUserId(), PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT));

            return buildWalletResponseDTO(updatedWallet, transactions);
        } else {
            // If discount type is DISCOUNT, it means the price itself is reduced,
            // not a direct cashback to the wallet. This logic would typically be in a ride/payment service.
            // For now, we'll throw an exception or return a success with no wallet change if the service only handles CASHBACK promos.
            throw new InvalidPromoException("Promo type is a discount, not cashback. Wallet balance not affected.");
        }
    }

    @Override
    public WalletResponseDTO initializeWallet(Long userId) {
        // Check if wallet already exists
        Optional<UserWallet> existingWallet = userWalletRepository.findByUserId(userId);
        if (existingWallet.isPresent()) {
            // You might return the existing wallet or throw an exception if creation is strictly once
            log.warn("Wallet already exists for user ID: {}. Returning existing wallet details.", userId);
            return getWalletDetails(userId);
        }

        UserWallet newWallet = UserWallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO) // New wallets start with zero balance
                .build();
        UserWallet savedWallet = userWalletRepository.save(newWallet);
        log.info("Initialized new wallet for user ID: {}. Wallet ID: {}", userId, savedWallet.getWalletId());
        return buildWalletResponseDTO(savedWallet, List.of()); // No transactions initially
    }

    private WalletResponseDTO buildWalletResponseDTO(UserWallet wallet, List<WalletTransaction> transactions) {
        List<TransactionDTO> transactionDTOS = transactions.stream()
                .map(this::mapToTransactionDTO)
                .collect(Collectors.toList());

        return WalletResponseDTO.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .lastUpdated(wallet.getUpdatedAt())
                .recentTransactions(transactionDTOS)
                .build();
    }

    private TransactionDTO mapToTransactionDTO(WalletTransaction transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType())
                .transactionFor(transaction.getTransactionFor())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}