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

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return productService.getAllCategories();
    }

     @GetMapping("/search/category")
    public List<String> getByCategory(@RequestParam String category) {
        return productService.getByCategory(category);
    }

     @GetMapping("/search/brand")
    public List<String> getByBrand(@RequestParam String brand) {
        return productService.getByBrand(brand);
    }

     @GetMapping("/search/name")
    public ProductResponse getByName(@RequestParam String name) {
        return productService.getByName(name);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
                                  @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }
    

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
