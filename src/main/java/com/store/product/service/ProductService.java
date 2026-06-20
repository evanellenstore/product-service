package com.store.product.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import com.store.product.dto.CategoryRequest;
import com.store.product.dto.CategoryResponse;
import com.store.product.dto.BrandRequest;
import com.store.product.dto.BrandResponse;
import com.store.product.dto.ProductRequest;
import com.store.product.dto.ProductResponse;
import com.store.product.entity.Product;
import com.store.product.exception.ProductException;
import com.store.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductResponse create(ProductRequest request) {

        // Get brand name from brand ID
        String brandName = null;
        if (request.getBrandId() != null) {
            try {
                brandName = brandService.getById(request.getBrandId()).getBrand();
            } catch (Exception e) {
                throw new ProductException("Invalid brand ID");
            }
        }

        String sku = generateSku(
            brandName,
            request.getCategory(),
            request.getName(),
            request.getUnit(),
            request.isLoose(),
            request.getProductSize(),
            request.getPacketSize(),
            request.getPacketUnit());

        if (productRepository.existsBySku(sku)) {
            throw new ProductException("Product SKU already exists");
        }

        // Check if external barcode already exists (if provided)
        if (request.getExternalBarcode() != null && !request.getExternalBarcode().isEmpty()) {
            if (productRepository.existsByExternalBarcode(request.getExternalBarcode())) {
                throw new ProductException("External barcode already exists");
            }
        }

        // generate barcode PNG for SKU
        byte[] barcodeBytes = null;
        try {
            int bw = 400;
            int bh = 100;
            BitMatrix bitMatrix = new MultiFormatWriter().encode(sku, BarcodeFormat.CODE_128, bw, bh);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            barcodeBytes = baos.toByteArray();
        } catch (Exception e) {
            // non-fatal — proceed without barcode
            barcodeBytes = null;
        }

        Product product = productRepository.save(Product.builder()
                .sku(sku)
                .externalBarcode(request.getExternalBarcode())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .brandId(request.getBrandId())
                .unit(request.getUnit())
                .loose(request.isLoose())
            .productSize(request.getProductSize())
            .packetSize(request.getPacketSize())
            .packetUnit(request.getPacketUnit())
                .price(request.getPrice())
                .discountAmount(request.getDiscountAmount())
                .status(request.getStatus())
                .barcode(barcodeBytes)
                .build());

        return mapToResponse(product);
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));
        return mapToResponse(product);
    }

    public List<ProductResponse> getByName(String name, String language) {

        if (name == null || name.isBlank()) {
            return List.of();
        }

        String[] searchNames = name.split("-");
        List<Product> products = List.of();

        if ("en".equalsIgnoreCase(language)) {
            String searchName = searchNames[0].trim();
            products = productRepository.fetchByName(searchName);

            // Fallback normalized English search
            if (products.isEmpty()) {
                String norm = searchName.replaceAll("[\\s\\-–—_]", "")
                        .toLowerCase();

                if (!norm.isBlank()) {
                    products = productRepository.fetchByNameNormalized(norm);
                }
            }

        } else if ("hi".equalsIgnoreCase(language)) {
             String searchName = searchNames[1].trim();

            products = productRepository.fetchByHindiName(searchName);

            // Fallback normalized Hindi search
            if (products.isEmpty()) {
                String norm = searchName.replaceAll("[\\s\\-–—_]", "");

                if (!norm.isBlank()) {
                    products = productRepository.fetchByHindiNameNormalized(norm);
                }
            }

        } else {

            // Search both English and Hindi
            String searchName = name.trim();
            products = productRepository.fetchByNameOrHindiName(searchName);

            if (products.isEmpty()) {
                String norm = searchName.replaceAll("[\\s\\-–—_]", "")
                        .toLowerCase();

                if (!norm.isBlank()) {
                    products = productRepository.fetchByNameOrHindiNameNormalized(norm);
                }
            }
        }

        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getAllSku(String sku) {

        Product product = productRepository.fetchBySku(sku);
        if (product == null) {
            throw new ProductException("Product not found");
        }
        return mapToResponse(product);
    }

    /**
     * Search product by barcode (either SKU or external barcode)
     * This method works for both SKU scanning and external barcode scanning
     */
    public ProductResponse searchByBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            throw new ProductException("Barcode cannot be empty");
        }

        // First try to find by external barcode
        var productByExternalBarcode = productRepository.findByExternalBarcode(barcode);
        if (productByExternalBarcode.isPresent()) {
            return mapToResponse(productByExternalBarcode.get());
        }

        // If not found, try to find by SKU
        Product product = productRepository.fetchBySku(barcode);
        if (product != null) {
            return mapToResponse(product);
        }

        throw new ProductException("Product not found for barcode: " + barcode);
    }

    public List<String> getByCategory(String category) {
        List<Long> brandIds = productRepository.findByCategory(category);
        if (brandIds.isEmpty()) {
            throw new ProductException("No products found");
        }
        // Convert brand IDs to brand names
        return brandIds.stream()
                .map(id -> {
                    try {
                        return brandService.getById(id).getBrand();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(name -> name != null)
                .toList();
    }

    public List<String> getByBrand(Long brandId) {
        List<String> skus = productRepository.findByBrand(brandId);
        if (skus.isEmpty()) {
            throw new ProductException("No products found for this brand");
        }
        return skus;
    }

    /**
     * Filter products by category, brand, and search term (for shopping/customer
     * view)
     * All parameters are optional. Returns only ACTIVE products.
     */
    public List<ProductResponse> filterProducts(Long categoryId, Long brandId, String searchTerm) {
        // Start with all ACTIVE products
        List<Product> products = productRepository.findByStatus("ACTIVE");

        // Filter by category if provided (must have matching brand in category mapping)
        if (categoryId != null && categoryId > 0) {
            // For now, we'll filter by products that have a brand
            // In a real scenario, we'd filter by products in categories
            products = products.stream()
                    .filter(p -> p.getBrandId() != null)
                    .toList();
        }

        // Filter by brand if provided
        if (brandId != null && brandId > 0) {
            products = products.stream()
                    .filter(p -> p.getBrandId() != null && p.getBrandId().equals(brandId))
                    .toList();
        }

        // Filter by search term if provided (search in name, description, SKU)
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerSearchTerm) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerSearchTerm))
                            ||
                            p.getSku().toLowerCase().contains(lowerSearchTerm))
                    .toList();
        }

        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));

        // Check if external barcode is being updated and if it already exists elsewhere
        if (request.getExternalBarcode() != null && !request.getExternalBarcode().isEmpty()) {
            String currentBarcode = product.getExternalBarcode();
            if (!request.getExternalBarcode().equals(currentBarcode) &&
                    productRepository.existsByExternalBarcode(request.getExternalBarcode())) {
                throw new ProductException("External barcode already exists");
            }
            product.setExternalBarcode(request.getExternalBarcode());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBrandId(request.getBrandId());
        product.setUnit(request.getUnit());
        product.setLoose(request.isLoose());
        product.setProductSize(request.getProductSize());
        product.setPacketSize(request.getPacketSize());
        product.setPacketUnit(request.getPacketUnit());
        product.setPrice(request.getPrice());
        product.setDiscountAmount(request.getDiscountAmount());
        product.setStatus(request.getStatus());

        return mapToResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        String barcodeBase64 = null;
        if (product.getBarcode() != null) {
            barcodeBase64 = java.util.Base64.getEncoder().encodeToString(product.getBarcode());
        }

        // Fetch brand name based on brand ID
        String brandName = null;
        if (product.getBrandId() != null) {
            try {
                brandName = brandService.getById(product.getBrandId()).getBrand();
            } catch (Exception e) {
                brandName = "Unknown";
            }
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .externalBarcode(product.getExternalBarcode())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brandId(product.getBrandId())
                .brandName(brandName)
                .unit(product.getUnit())
                .productSize(product.getProductSize())
                .loose(product.isLoose())
                .packetSize(product.getPacketSize())
                .packetUnit(product.getPacketUnit())
                .price(product.getPrice())
                .discountAmount(product.getDiscountAmount())
                .status(product.getStatus())
                .barcode(barcodeBase64)
                .build();
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        return categoryService.create(request);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAll();
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryService.getActive();
    }

    public CategoryResponse toggleCategoryStatus(Long categoryId) {
        return categoryService.toggleActive(categoryId);
    }

    public CategoryResponse updateCategoryStatus(Long categoryId, Boolean isActive) {
        return categoryService.updateStatus(categoryId, isActive);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        return categoryService.update(categoryId, request);
    }

    public BrandResponse createBrand(BrandRequest request) {
        return brandService.create(request);
    }

    public List<BrandResponse> getAllBrands() {
        return brandService.getAll();
    }

    public List<BrandResponse> getActiveBrands() {
        return brandService.getActive();
    }

    public BrandResponse updateBrand(Long brandId, BrandRequest request) {
        return brandService.update(brandId, request);
    }

    public BrandResponse toggleBrandStatus(Long brandId) {
        return brandService.toggleActive(brandId);
    }

    public void deleteBrand(Long brandId) {
        brandService.delete(brandId);
    }

    public List<String> getBrandsByCategory(String category) {
        List<Long> brandIds = productRepository.findByCategory(category);
        // Convert brand IDs to brand names
        return brandIds.stream()
                .map(id -> {
                    try {
                        return brandService.getById(id).getBrand();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(name -> name != null)
                .toList();
    }

    public String generateSku(String brand, String category, String name, String unit, boolean loose, Double productSize, Double packetSize, String packetUnit) {

        String type = "";
       if(loose) {
            // when sold loose, include productSize and unit in SKU unit string if available
            if (productSize != null && unit != null) {
                unit = String.format("%s%s", productSize, unit);
            }
            type = "L";
        } else if ( packetSize != null && packetUnit != null) {
            unit = String.format("%s%s", packetSize, packetUnit);
            type= "P";

        }
       
       
       
        String base = String.join("|",
                normalize(brand),
                normalize(category),
                normalize(name),
                normalize(unit),
                normalize(type));

        String hash = shortHash(base);

        return String.format(
                "%s-%s-%s-%s-%s",
                shortCode(brand),
                shortCode(name),
                unit.toUpperCase(Locale.ROOT),
                shortCode(type),
                hash);
    }

    private String shortCode(String value) {
        if (value == null)
            return "NA";
        value = value.replaceAll("[^a-zA-Z0-9]", "");
        return value.length() <= 6
                ? value.toUpperCase()
                : value.substring(0, 6).toUpperCase();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String shortHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 3; i++) { // 6 hex chars
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SKU generation failed", e);
        }
    }

}
