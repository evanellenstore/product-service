package com.store.product.repository;

import com.store.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // Find only active brands
    List<Brand> findByIsActiveTrue();

    // Find all brands (including inactive) for admin
    List<Brand> findAll();
}
