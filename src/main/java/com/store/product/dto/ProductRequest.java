package com.store.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductRequest {

    private String sku;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String unit;
    private Double price;
    private String status;
}
