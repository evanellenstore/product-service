package com.store.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.store.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.category = :category")
    List<String> findByCategory(String category);

    @Query("SELECT DISTINCT p.name FROM Product p WHERE p.brand = :brand")
    List<String> findByBrand(String brand);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> getAllCategories();

    boolean existsBySku(String sku);
    Optional<Product> findByName(String name);
}
