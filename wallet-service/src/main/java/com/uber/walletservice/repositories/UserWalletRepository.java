package com.uber.walletservice.repositories;

import com.uber.walletservice.models.UserWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
    Optional<UserWallet> findByUserId(Long userId);

    // Using PESSIMISTIC_WRITE lock to prevent race conditions during balance updates
    // when using methods like save()
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uw FROM UserWallet uw WHERE uw.userId = :userId")
    Optional<UserWallet> findByUserIdWithLock(@Param("userId") Long userId);
}