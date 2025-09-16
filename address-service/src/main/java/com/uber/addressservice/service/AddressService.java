package com.uber.addressservice.service;

import com.uber.addressservice.dto.AddressRequest;
import com.uber.addressservice.dto.AddressResponse;
import com.uber.addressservice.exception.AddressNotFoundException;
import com.uber.addressservice.model.Address;
import com.uber.addressservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressResponse createAddress(AddressRequest request) {
        // You might want to add logic here to check for duplicate addresses
        // Example: if (addressRepository.findByLine1AndCityAndPostalCode(request.getLine1(), request.getCity(), request.getPostalCode()).isPresent()) {
        //     throw new DuplicateAddressException("Address with same details already exists.");
        // }

        Address address = Address.builder()
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        Address savedAddress = addressRepository.save(address);
        return mapToAddressResponse(savedAddress);
    }

    public AddressResponse getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with ID: " + id));
        return mapToAddressResponse(address);
    }

    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with ID: " + id));

        // Update fields
        existingAddress.setLine1(request.getLine1());
        existingAddress.setLine2(request.getLine2());
        existingAddress.setCity(request.getCity());
        existingAddress.setState(request.getState());
        existingAddress.setCountry(request.getCountry());
        existingAddress.setPostalCode(request.getPostalCode());
        existingAddress.setLatitude(request.getLatitude());
        existingAddress.setLongitude(request.getLongitude());
        existingAddress.setRole(request.getRole()); // Role can also be updated

        Address updatedAddress = addressRepository.save(existingAddress);
        return mapToAddressResponse(updatedAddress);
    }

    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException("Address not found with ID: " + id);
        }
        addressRepository.deleteById(id);
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .role(address.getRole())
                .userId(address.getUserId())
                .build();
    }
}