package com.store.product.controller;

import com.store.product.dto.*;
import com.store.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.ByteArrayOutputStream;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

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
    public List<ProductResponse> getByName(@RequestParam String name) {
        return productService.getByName(name);
    }

    @GetMapping("/search/sku")
    public ProductResponse getBySku(@RequestParam String sku) {
        return productService.getAllSku(sku);
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

    /**
     * Get only active product categories (for inventory dropdown).
     */
    @GetMapping("/categories/active")
    public List<CategoryResponse> getActiveCategories() {
        return productService.getActiveCategories();
    }

    /**
     * Toggle category active/inactive status (admin only).
     */
    @PutMapping("/categories/{id}/toggle")
    public CategoryResponse toggleCategoryStatus(@PathVariable Long id) {
        return productService.toggleCategoryStatus(id);
    }

    /**
     * Update category active status (admin only).
     */
    @PutMapping("/categories/{id}/status")
    public CategoryResponse updateCategoryStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return productService.updateCategoryStatus(id, isActive);
    }

    /**
     * Create a new product brand.
     */
    @PostMapping("/brand")
    public BrandResponse createBrand(@RequestBody BrandRequest request) {
        return productService.createBrand(request);
    }

    /**
     * Get all product brands.
     */
    @GetMapping("/brands")
    public List<BrandResponse> getAllBrands() {
        return productService.getAllBrands();
    }

    /**
     * Get brands by category.
     */
    @GetMapping("/brands/category/{category}")
    public List<String> getBrandsByCategory(@PathVariable String category) {
        return productService.getBrandsByCategory(category);
    }

    /**
     * Update a brand.
     */
    @PutMapping("/brand/{id}")
    public BrandResponse updateBrand(@PathVariable Long id, @RequestBody BrandRequest request) {
        return productService.updateBrand(id, request);
    }

    /**
     * Toggle brand active/inactive status.
     */
    @PutMapping("/brand/{id}/toggle")
    public BrandResponse toggleBrandStatus(@PathVariable Long id) {
        return productService.toggleBrandStatus(id);
    }

    /**
     * Delete a brand.
     */
    @DeleteMapping("/brand/{id}")
    public void deleteBrand(@PathVariable Long id) {
        productService.deleteBrand(id);
    }

        /**
         * Generate a Code128 barcode PNG for a given SKU.
         */
        @GetMapping(value = "/sku/{sku}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
        public ResponseEntity<byte[]> barcodeForSku(@PathVariable String sku) throws Exception {
            int width = 400;
            int height = 100;

            BitMatrix bitMatrix = new MultiFormatWriter().encode(sku, BarcodeFormat.CODE_128, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            byte[] img = baos.toByteArray();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(img);
        }

}
