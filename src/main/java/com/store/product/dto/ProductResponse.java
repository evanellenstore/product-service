package com.store.product.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String unit;
    private Double price;
    private String status;
    private String barcode; // Base64 PNG string, optional
}
