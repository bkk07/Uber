package com.uber.addressservice.controller;
import com.uber.addressservice.dto.AddressRequest;
import com.uber.addressservice.dto.AddressResponse;
import com.uber.addressservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        long userId = Long.parseLong(authentication.getPrincipal().toString());
        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()   // or filter if you have multiple
                .orElse("UNKNOWN");
        System.out.println("UserId: " + userId + ", Role: " + role);
        request.setUserId(userId);
        request.setRole(role);
        AddressResponse createdAddress = addressService.createAddress(request);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Long id) {
        AddressResponse address = addressService.getAddressById(id);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        List<AddressResponse> addresses = addressService.getAllAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        AddressResponse updatedAddress = addressService.updateAddress(id, request);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}