package com.uber.adminservice;

import com.uber.adminservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUsername(String username);
}
