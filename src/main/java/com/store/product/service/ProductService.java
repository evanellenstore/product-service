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
                request.getUnit());

        if (productRepository.existsBySku(sku)) {
            throw new ProductException("Product SKU already exists");
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
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .brandId(request.getBrandId())
                .unit(request.getUnit())
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

    public List<ProductResponse> getByName(String name) {
        List<Product> products = productRepository.fetchByName(name);
        if (products.isEmpty()) {
            throw new ProductException("Product not found");
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

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBrandId(request.getBrandId());
        product.setUnit(request.getUnit());
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
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brandId(product.getBrandId())
                .brandName(brandName)
                .unit(product.getUnit())
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

     public String generateSku(String brand, String category, String name, String unit) {

        String base = String.join("|",
                normalize(brand),
                normalize(category),
                normalize(name),
                normalize(unit)
        );

        String hash = shortHash(base);

        return String.format(
                "%s-%s-%s-%s",
                shortCode(brand),
                shortCode(name),
                unit.toUpperCase(Locale.ROOT),
                hash
        );
    }

    private String shortCode(String value) {
        if (value == null) return "NA";
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
