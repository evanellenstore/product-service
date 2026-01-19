package com.store.product.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.store.product.dto.CategoryRequest;
import com.store.product.dto.CategoryResponse;
import com.store.product.dto.ProductRequest;
import com.store.product.dto.ProductResponse;
import com.store.product.entity.Category;
import com.store.product.entity.Product;
import com.store.product.exception.ProductException;
import com.store.product.repository.CategoryRepository;
import com.store.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;;

    

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductException("Product SKU already exists");
        }

        Product product = productRepository.save(Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .brand(request.getBrand())
                .unit(request.getUnit())
                .price(request.getPrice())
                .status(request.getStatus())
                .build());

        return mapToResponse(product);
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
        return mapToResponse(product);
    }

    public ProductResponse getByName(String name) {
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductException("Product not found"));
        return mapToResponse(product);
    }

    

    public List<String> getByCategory(String category) {
        List<String> brands = productRepository.findByCategory(category);
        if (brands.isEmpty()) {
            throw new ProductException("No products found");
        }
        return brands;
    }

    public List<String> getByBrand(String brand) {
        List<String> names = productRepository.findByBrand(brand);
        if (names.isEmpty()) {
            throw new ProductException("No products found");
        }
        return names;
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());

        return mapToResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .unit(product.getUnit())
                .price(product.getPrice())
                .status(product.getStatus())
                .build();
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryRepository.save(Category.builder()
                .name(request.getCategory())
                .build());
        return new CategoryResponse(category.getId(), category.getName());
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .toList();
    }
}
