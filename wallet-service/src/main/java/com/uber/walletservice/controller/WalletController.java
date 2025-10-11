package com.uber.walletservice.controller;
import com.uber.walletservice.dtos.AddMoneyRequestDTO;
import com.uber.walletservice.dtos.ApplyPromoRequestDTO;
import com.uber.walletservice.dtos.DeductMoneyRequestDTO;
import com.uber.walletservice.dtos.WalletResponseDTO;
import com.uber.walletservice.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;
    //Get wallet by the user Id
    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponseDTO> getWallet(@PathVariable Long userId) {
        log.info("Received request to get wallet details for user ID: {}", userId);
        WalletResponseDTO wallet = walletService.getWalletDetails(userId);
        return ResponseEntity.ok(wallet);
    }
    // Adding money
    @PostMapping("/add-money")
    public ResponseEntity<WalletResponseDTO> addMoneyToWallet(@Valid @RequestBody AddMoneyRequestDTO request) {
        log.info("Received request to add money for user ID: {}", request.getUserId());
        WalletResponseDTO updatedWallet = walletService.addMoney(request);
        return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
    }
    @PostMapping("/deduct-money")
    public ResponseEntity<WalletResponseDTO> deductMoneyFromWallet(@Valid @RequestBody DeductMoneyRequestDTO request) {
        log.info("Received request to deduct money for user ID: {}", request.getUserId());
        WalletResponseDTO updatedWallet = walletService.deductMoney(request);
        return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
    }

    @PostMapping("/apply-promo")
    public ResponseEntity<WalletResponseDTO> applyPromoCode(@Valid @RequestBody ApplyPromoRequestDTO request) {
        log.info("Received request to apply promo code '{}' for user ID: {}", request.getPromoCode(), request.getUserId());
        WalletResponseDTO updatedWallet = walletService.applyPromo(request);
        return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
    }
    // This endpoint could be called by a User Management Service upon new user registration
    @PostMapping("/initialize/{userId}")
    public ResponseEntity<WalletResponseDTO> initializeUserWallet(@PathVariable Long userId) {
        log.info("Received request to initialize wallet for new user ID: {}", userId);
        WalletResponseDTO initializedWallet = walletService.initializeWallet(userId);
        return new ResponseEntity<>(initializedWallet, HttpStatus.CREATED);
    }
}