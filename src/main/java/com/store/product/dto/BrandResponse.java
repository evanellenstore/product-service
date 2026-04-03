package com.store.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrandResponse {
    private Long id;
    private String brand;
    private Boolean isActive;
}
