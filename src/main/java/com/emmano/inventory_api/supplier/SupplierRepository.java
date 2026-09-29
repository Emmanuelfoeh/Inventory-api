package com.emmano.inventory_api.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Supplier> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
