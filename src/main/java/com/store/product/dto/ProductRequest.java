package com.store.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductRequest {
   
    private String name;
    private String description;
    private String category;
    private Long brandId;
    private String unit;
    private Double price;
    private String status;
    // barcode size is fixed on server (no client-specified sizing)
}
