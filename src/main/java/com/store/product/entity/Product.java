package com.store.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "product",
    uniqueConstraints = @UniqueConstraint(columnNames = "sku")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    private String category;

    @Column(name = "brand_id")
    private Long brandId;

    private String unit;   // PCS / KG / LTR

    private Double price;

    @Column(nullable = false)
    private String status; // ACTIVE / INACTIVE

    @Lob
    @Column(name = "barcode", columnDefinition = "LONGBLOB")
    private byte[] barcode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
