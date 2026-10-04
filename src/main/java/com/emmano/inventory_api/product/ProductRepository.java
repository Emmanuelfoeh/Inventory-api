package com.emmano.inventory_api.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    Page<Product> findAllByActiveTrue(Pageable pageable);
    Optional<Product> findByIdAndActiveTrue(Long id);
}
