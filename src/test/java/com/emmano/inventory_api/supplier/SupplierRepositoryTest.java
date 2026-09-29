package com.emmano.inventory_api.supplier;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SupplierRepositoryTest {


    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierRepositoryTest(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Test
    void shouldSaveAndFindSupplierByEmailIgnoringCase() {
        String email = "sales-" + UUID.randomUUID() + "@example.com";

        Supplier supplier = new Supplier(
                "Example Electronics",
                email,
                "+233 20 123 4567"
        );

        Supplier savedSupplier =
                supplierRepository.saveAndFlush(supplier);

        Optional<Supplier> result =
                supplierRepository.findByEmailIgnoreCase(email.toUpperCase());

        assertThat(savedSupplier.getId()).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("Example Electronics");
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getPhone())
                .isEqualTo("+233 20 123 4567");
        assertThat(result.get().getCreatedAt()).isNotNull();
    }
}
