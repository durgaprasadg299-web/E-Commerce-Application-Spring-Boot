package com.hendisantika.controller;

import com.hendisantika.dto.ProductRequest;
import com.hendisantika.dto.ProductResponse;
import com.hendisantika.model.Product;
import com.hendisantika.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductApiController {
    private final ProductService productService;

    @GetMapping("/brands")
    public List<String> getBrands() {
        return productService.getAllBrands();
    }

    @GetMapping("/brands/{brand}/products")
    public List<ProductResponse> getProductsByBrand(@PathVariable String brand) {
        return productService.getCatalogProducts(null, brand, null, null).stream()
                .map(ProductApiController::toResponse)
                .toList();
    }

    @GetMapping("/products")
    public List<ProductResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double maxPrice) {
        return productService.getCatalogProducts(search, brand, categoryId, maxPrice).stream()
                .map(ProductApiController::toResponse)
                .toList();
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return toResponse(productService.getProductById(id));
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return toResponse(productService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id,
                                         @Valid @RequestBody ProductRequest request) {
        return toResponse(productService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
    }

    private static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getQuantity(),
                product.getPrice(),
                product.getBrand(),
                product.getDiscount() == null ? 0 : product.getDiscount().getDiscount(),
                product.getImage(),
                product.getCategories().stream()
                        .map(category -> category.getName())
                        .collect(java.util.stream.Collectors.toUnmodifiableSet())
        );
    }
}
