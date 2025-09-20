package com.uber.walletservice.repositories;

import com.uber.walletservice.models.WalletTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserWallet_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}