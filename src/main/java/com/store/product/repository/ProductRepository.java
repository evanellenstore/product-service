package com.store.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.store.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT DISTINCT p.brandId FROM Product p WHERE p.category = :category")
    List<Long> findByCategory(String category);

    @Query("SELECT DISTINCT p.sku FROM Product p WHERE p.brandId = :brandId")
    List<String> findByBrand(Long brandId);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> getAllCategories();

    boolean existsBySku(String sku);

    Optional<Product> findByName(String name);

    @Query("SELECT p FROM Product p WHERE p.name = :name")
    List<Product> fetchByName(String name);

    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Product fetchBySku(String sku);
}
