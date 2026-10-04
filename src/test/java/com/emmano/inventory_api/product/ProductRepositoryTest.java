package com.emmano.inventory_api.product;

import com.emmano.inventory_api.category.Category;
import com.emmano.inventory_api.category.CategoryRepository;
import com.emmano.inventory_api.supplier.Supplier;
import com.emmano.inventory_api.supplier.SupplierRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ProductRepositoryTest {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    ProductRepositoryTest(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Test
    void shouldSaveProductWithCategoryAndSupplier() {
        String uniqueValue = UUID.randomUUID().toString();

        Category category = categoryRepository.saveAndFlush(
                new Category(
                        "Computers-" + uniqueValue,
                        "Computers and accessories"
                )
        );

        Supplier supplier = supplierRepository.saveAndFlush(
                new Supplier(
                        "Technology Supplier",
                        "sales-" + uniqueValue + "@example.com",
                        "+233 20 123 4567"
                )
        );

        String sku = "LAPTOP-" + uniqueValue;

        Product product = new Product(
                "Learning Laptop",
                sku,
                "A laptop created during the persistence lesson",
                new BigDecimal("1250.00"),
                category,
                supplier
        );

        Product savedProduct = productRepository.saveAndFlush(product);

        Optional<Product> result = productRepository.findBySkuIgnoreCase(
                sku.toLowerCase(Locale.ROOT)
        );

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(result).isPresent();

        Product foundProduct = result.get();

        assertThat(foundProduct.getName()).isEqualTo("Learning Laptop");
        assertThat(foundProduct.getPrice())
                .isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(foundProduct.getCategory().getId())
                .isEqualTo(category.getId());
        assertThat(foundProduct.getSupplier().getId())
                .isEqualTo(supplier.getId());
        assertThat(foundProduct.isActive()).isTrue();
        assertThat(foundProduct.getCreatedAt()).isNotNull();
        assertThat(foundProduct.getUpdatedAt()).isNotNull();
    }
}
