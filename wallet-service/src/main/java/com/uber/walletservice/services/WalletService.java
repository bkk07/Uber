package com.uber.walletservice.services;

import com.uber.walletservice.dtos.AddMoneyRequestDTO;
import com.uber.walletservice.dtos.ApplyPromoRequestDTO;
import com.uber.walletservice.dtos.DeductMoneyRequestDTO;
import com.uber.walletservice.dtos.WalletResponseDTO;

public interface WalletService {
    /**
     * Retrieves a user's wallet balance and recent transactions.
     * @param userId The ID of the user.
     * @return WalletResponseDTO containing wallet details.
     */
    WalletResponseDTO getWalletDetails(Long userId);
    /**
     * Adds money to a user's wallet.
     * This is typically called after a successful payment confirmation from a Payment Service.
     * @param request The AddMoneyRequestDTO containing user ID, amount, and payment reference.
     * @return WalletResponseDTO with updated wallet details.
     */

    WalletResponseDTO addMoney(AddMoneyRequestDTO request);
    /**
     * Deducts money from a user's wallet for a ride or other payment.
     * @param request The DeductMoneyRequestDTO containing user ID, amount, and ride reference.
     * @return WalletResponseDTO with updated wallet details.
     */
    WalletResponseDTO deductMoney(DeductMoneyRequestDTO request);

    /**
     * Applies a promo code, which might result in cashback credited to the wallet.
     * @param request The ApplyPromoRequestDTO containing user ID and promo code.
     * @return WalletResponseDTO with updated wallet details after applying cashback.
     */
    WalletResponseDTO applyPromo(ApplyPromoRequestDTO request);

    /**
     * Initializes a wallet for a new user.
     * @param userId The ID of the user.
     * @return The created UserWallet entity.
     */
    WalletResponseDTO initializeWallet(Long userId); // Utility for new user creation
}