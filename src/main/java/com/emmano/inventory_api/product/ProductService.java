package com.emmano.inventory_api.product;

import com.emmano.inventory_api.category.Category;
import com.emmano.inventory_api.category.CategoryRepository;
import com.emmano.inventory_api.common.exception.DuplicateSkuException;
import com.emmano.inventory_api.common.exception.ResourceNotFoundException;
import com.emmano.inventory_api.product.dto.CreateProductRequest;
import com.emmano.inventory_api.product.dto.ProductResponse;
import com.emmano.inventory_api.product.dto.UpdateProductRequest;
import com.emmano.inventory_api.supplier.Supplier;
import com.emmano.inventory_api.supplier.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String normalisedSku = normalizeSku(request.sku());
        if (productRepository.existsBySkuIgnoreCase(normalisedSku)) {
            throw new DuplicateSkuException(normalisedSku);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category with id " + request.categoryId() + " was not found"));
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier with id " + request.supplierId() + " was not found"));

        Product product = new Product(
                request.name().trim(),
                normalisedSku,
                normalizeDescription(request.description()),
                request.price(),
                category,
                supplier);
        return toResponse(productRepository.save(product));

    }

    // public Page<ProductResponse> getProducts(Pageable pageable) {
    //     return productRepository.findAll(pageable).map(this::toResponse);
    // }

    // public ProductResponse getProduct(Long id) {
    //     Product product = productRepository.findById(id)
    //             .orElseThrow(() -> new ResourceNotFoundException("Product with id:" + id + " was not found"));
    //     return toResponse(product);
    // }

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable).map(this::toResponse);
    }

    public ProductResponse getProduct(Long id) {
        return toResponse(findActiveProduct(id));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findActiveProduct(id);
        String name = request.name() == null ? product.getName() : request.name().trim();
        String description = request.description() == null ? product.getDescription()
                : normalizeDescription(request.description());

        BigDecimal price = request.price() == null ? product.getPrice() : request.price();

        Supplier supplier = request.supplierId() == null ? product.getSupplier()
                : findSupplier(request.supplierId());
        Category category = request.categoryId() == null ? product.getCategory()
                : findCategory(request.categoryId());

        product.updateDetails(name, description, price, category, supplier);
        productRepository.flush();
        return toResponse(product);

    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findActiveProduct(id);
        product.deactivate();

    }

    private Product findActiveProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " was not found"));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " was not found"));
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " was not found"));
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmedDescription = description.trim();
        return trimmedDescription.isEmpty() ? null : trimmedDescription;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getSupplier().getId(),
                product.getSupplier().getName(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
