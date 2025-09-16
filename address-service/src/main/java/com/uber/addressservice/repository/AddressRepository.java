package com.uber.addressservice.repository;

import com.uber.addressservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

}