package com.emmano.inventory_api.product;

import com.emmano.inventory_api.category.Category;
import com.emmano.inventory_api.category.CategoryRepository;
import com.emmano.inventory_api.common.exception.DuplicateSkuException;
import com.emmano.inventory_api.common.exception.ResourceNotFoundException;
import com.emmano.inventory_api.product.dto.CreateProductRequest;
import com.emmano.inventory_api.product.dto.ProductResponse;
import com.emmano.inventory_api.supplier.Supplier;
import com.emmano.inventory_api.supplier.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductServiceTest {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    ProductServiceTest(
            ProductService productService,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository
    ) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Test
    void shouldCreateProduct() {
        String uniqueValue = UUID.randomUUID().toString();

        Category category = categoryRepository.saveAndFlush(
                new Category(
                        "Laptops-" + uniqueValue,
                        "Portable computers"
                )
        );

        Supplier supplier = supplierRepository.saveAndFlush(
                new Supplier(
                        "Laptop Supplier",
                        "orders-" + uniqueValue + "@example.com",
                        "+233 20 123 4567"
                )
        );

        CreateProductRequest request = new CreateProductRequest(
                "  Learning Laptop  ",
                "  laptop-" + uniqueValue + "  ",
                "  A laptop for learning Spring Boot  ",
                new BigDecimal("1250.00"),
                category.getId(),
                supplier.getId()
        );

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Learning Laptop");
        assertThat(response.sku()).startsWith("LAPTOP-");
        assertThat(response.description())
                .isEqualTo("A laptop for learning Spring Boot");
        assertThat(response.price())
                .isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(response.categoryId()).isEqualTo(category.getId());
        assertThat(response.categoryName()).isEqualTo(category.getName());
        assertThat(response.supplierId()).isEqualTo(supplier.getId());
        assertThat(response.supplierName()).isEqualTo(supplier.getName());
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateSkuIgnoringCaseAndWhitespace() {
        String uniqueValue = UUID.randomUUID().toString();

        Category category = categoryRepository.saveAndFlush(
                new Category(
                        "Laptops-" + uniqueValue,
                        "Portable computers"
                )
        );

        Supplier supplier = supplierRepository.saveAndFlush(
                new Supplier(
                        "Laptop Supplier",
                        "orders-" + uniqueValue + "@example.com",
                        "+233 20 123 4567"
                )
        );

        String sku = "LAPTOP-" + uniqueValue;

        productService.createProduct(new CreateProductRequest(
                "First Laptop",
                sku,
                null,
                new BigDecimal("1250.00"),
                category.getId(),
                supplier.getId()
        ));

        CreateProductRequest duplicateRequest = new CreateProductRequest(
                "Second Laptop",
                "  " + sku.toLowerCase(Locale.ROOT) + "  ",
                null,
                new BigDecimal("1500.00"),
                category.getId(),
                supplier.getId()
        );

        assertThatThrownBy(() -> productService.createProduct(duplicateRequest))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessage(
                        "A product with sku "
                                + sku.toUpperCase(Locale.ROOT)
                                + " already exists"
                );
    }

    @Test
    void shouldRejectProductWhenCategoryDoesNotExist() {
        String uniqueValue = UUID.randomUUID().toString();

        Supplier supplier = supplierRepository.saveAndFlush(
                new Supplier(
                        "Laptop Supplier",
                        "orders-" + uniqueValue + "@example.com",
                        "+233 20 123 4567"
                )
        );

        Long missingCategoryId = Long.MAX_VALUE;
        CreateProductRequest request = new CreateProductRequest(
                "Learning Laptop",
                "LAPTOP-" + uniqueValue,
                null,
                new BigDecimal("1250.00"),
                missingCategoryId,
                supplier.getId()
        );

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category with id " + missingCategoryId + " was not found");
    }

    @Test
    void shouldRejectProductWhenSupplierDoesNotExist() {
        String uniqueValue = UUID.randomUUID().toString();

        Category category = categoryRepository.saveAndFlush(
                new Category(
                        "Laptops-" + uniqueValue,
                        "Portable computers"
                )
        );

        Long missingSupplierId = Long.MAX_VALUE;
        CreateProductRequest request = new CreateProductRequest(
                "Learning Laptop",
                "LAPTOP-" + uniqueValue,
                null,
                new BigDecimal("1250.00"),
                category.getId(),
                missingSupplierId
        );

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Supplier with id " + missingSupplierId + " was not found");
    }
}
