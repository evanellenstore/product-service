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
    private Double discountAmount;
    private String status;
    private String externalBarcode; // External barcode number (can be scanned)
    private boolean loose; // true = sold loose/bulk, false = packaged unit
    private Double packetSize; // numeric size of the packet
    private String packetUnit; // unit of the packet contents (KG, G, LTR, ML)
}
