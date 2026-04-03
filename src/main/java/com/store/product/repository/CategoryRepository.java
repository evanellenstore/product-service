package com.store.product.repository;

import com.store.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // Find only active categories
    List<Category> findByIsActiveTrue();

    // Find all categories (including inactive) for admin
    List<Category> findAll();
}
