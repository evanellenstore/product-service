package com.store.product.controller;

import com.store.product.dto.*;
import com.store.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product.
     */
    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /**
     * Create multiple new products.
     */
    @PostMapping("/bulk")
    public String createAll(@RequestBody List<ProductRequest> requests) {
        for (ProductRequest request : requests) {
            productService.create(request);
        }
        return "done";
    }

    /**
     * Get all products.
     */
    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    /**
     * Get a product by ID.
     */
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /**
     * Get products by category.
     */
    @GetMapping("/search/category")
    public List<String> getByCategory(@RequestParam String category) {
        return productService.getByCategory(category);
    }

    /**
     * Get products by brand.
     */
    @GetMapping("/search/brand")
    public List<String> getByBrand(@RequestParam String brand) {
        return productService.getByBrand(brand);
    }

    /**
     * Get products by name.
     */
    @GetMapping("/search/name")
    public ProductResponse getByName(@RequestParam String name) {
        return productService.getByName(name);
    }

    /**
     * Update a product.
     */
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
            @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    /**
     * Delete a product.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    /**
     * Create a new product category.
     */
    @PostMapping("/category")
    public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
        return productService.createCategory(request);
    }

    /**
     * Create multiple product categories.
     */
    @PostMapping("/category/bulk")
    public String createAllCategories(@RequestBody List<CategoryRequest> requests) {
        for (CategoryRequest request : requests) {
            productService.createCategory(request);
        }
        return "done";
    }

    /**
     * Get all product categories.
     */
    @GetMapping("/categories")
    public List<CategoryResponse> getAllCategories() {
        return productService.getAllCategories();
    }

}
