package com.emmano.inventory_api.product;

import com.emmano.inventory_api.category.Category;
import com.emmano.inventory_api.category.CategoryRepository;
import com.emmano.inventory_api.supplier.Supplier;
import com.emmano.inventory_api.supplier.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    private final MockMvc mockMvc;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    ProductControllerTest(
            MockMvc mockMvc,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository) {
        this.mockMvc = mockMvc;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Test
    void shouldCompleteProductCrudLifecycle() throws Exception {
        TestReferences references = createReferences();
        String sku = ("LAPTOP-" + UUID.randomUUID())
                .toUpperCase(Locale.ROOT);

        String createRequest = """
                {
                  "name": "Learning Laptop",
                  "sku": "%s",
                  "description": "A laptop for learning Spring Boot",
                  "price": 1250.00,
                  "categoryId": %d,
                  "supplierId": %d
                }
                """.formatted(
                sku,
                references.category().getId(),
                references.supplier().getId());

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Learning Laptop"))
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.active").value(true));

        Long productId = productRepository
                .findBySkuIgnoreCase(sku)
                .orElseThrow()
                .getId();

        mockMvc.perform(get(
                "/api/v1/products/{id}",
                productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.categoryId")
                        .value(references.category().getId()))
                .andExpect(jsonPath("$.supplierId")
                        .value(references.supplier().getId()));

        mockMvc.perform(get("/api/v1/products")
                .param("size", "100")
                .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content[*].sku",
                        hasItem(sku)));

        String updateRequest = """
                {
                  "name": "Updated Learning Laptop",
                  "description": "   ",
                  "price": 1350.00
                }
                """;

        mockMvc.perform(patch(
                "/api/v1/products/{id}",
                productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Updated Learning Laptop"))
                .andExpect(jsonPath("$.description")
                        .value(nullValue()))
                .andExpect(jsonPath("$.price")
                        .value(1350.00))
                .andExpect(jsonPath("$.sku").value(sku));

        mockMvc.perform(delete(
                "/api/v1/products/{id}",
                productId))
                .andExpect(status().isNoContent());

        Product deletedProduct = productRepository
                .findById(productId)
                .orElseThrow();

        assertThat(deletedProduct.isActive()).isFalse();

        mockMvc.perform(get(
                "/api/v1/products/{id}",
                productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Product with id "
                                + productId
                                + " was not found"));

        mockMvc.perform(get("/api/v1/products")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content[*].sku",
                        not(hasItem(sku))));
    }

    @Test
    void shouldReturnValidationErrorsForInvalidCreate()
            throws Exception {
        String request = """
                {
                  "name": " ",
                  "sku": " ",
                  "price": -1,
                  "categoryId": 0,
                  "supplierId": 0
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath(
                        "$.validationErrors.name").exists())
                .andExpect(jsonPath(
                        "$.validationErrors.sku").exists())
                .andExpect(jsonPath(
                        "$.validationErrors.price").exists())
                .andExpect(jsonPath(
                        "$.validationErrors.categoryId").exists())
                .andExpect(jsonPath(
                        "$.validationErrors.supplierId").exists());
    }

    @Test
    void shouldRejectUpdateWithoutFields() throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"));
    }

    @Test
    void shouldReturnConflictForDuplicateSku()
            throws Exception {
        TestReferences references = createReferences();
        String sku = ("LAPTOP-" + UUID.randomUUID())
                .toUpperCase(Locale.ROOT);

        String firstRequest = productRequestJson(
                "First Laptop",
                sku,
                references);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isCreated());

        String duplicateRequest = productRequestJson(
                "Second Laptop",
                "  " + sku.toLowerCase(Locale.ROOT) + "  ",
                references);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A product with sku "
                                + sku
                                + " already exists"));
    }

    @Test
    void shouldReturnNotFoundForMissingProduct()
            throws Exception {
        mockMvc.perform(get(
                "/api/v1/products/{id}",
                Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private TestReferences createReferences() {
        String uniqueValue = UUID.randomUUID().toString();

        Category category = categoryRepository.saveAndFlush(
                new Category(
                        "Laptops-" + uniqueValue,
                        "Portable computers"));

        Supplier supplier = supplierRepository.saveAndFlush(
                new Supplier(
                        "Laptop Supplier",
                        "orders-" + uniqueValue + "@example.com",
                        "+233 20 123 4567"));

        return new TestReferences(category, supplier);
    }

    private String productRequestJson(
            String name,
            String sku,
            TestReferences references) {
        return """
                {
                  "name": "%s",
                  "sku": "%s",
                  "price": 1250.00,
                  "categoryId": %d,
                  "supplierId": %d
                }
                """.formatted(
                name,
                sku,
                references.category().getId(),
                references.supplier().getId());
    }

    private record TestReferences(
            Category category,
            Supplier supplier) {
    }
}