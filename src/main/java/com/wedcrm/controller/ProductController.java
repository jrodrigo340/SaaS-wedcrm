package com.wedcrm.controller;

import com.wedcrm.dto.request.ProductRequestDTO;
import com.wedcrm.dto.response.ProductResponseDTO;
import com.wedcrm.dto.dashboard.ProductSummaryDTO;
import com.wedcrm.entity.Product;
import com.wedcrm.mapper.ProductMapper;
import com.wedcrm.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts(
            @RequestParam(required = false) Boolean activeOnly) {

        List<Product> products = Boolean.TRUE.equals(activeOnly)
                ? productService.getAllActiveProducts()
                : productService.searchProducts("");

        List<ProductSummaryDTO> dtos = products.stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(productMapper.toResponseDTO(product));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO request) {
        Product product = productMapper.toEntity(request);
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toResponseDTO(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDTO request) {

        Product product = productMapper.toEntity(request);
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(productMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> activateProduct(@PathVariable UUID id) {
        productService.activateProduct(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deactivateProduct(@PathVariable UUID id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchProducts(
            @RequestParam String q) {
        List<Product> products = productService.searchProducts(q);
        List<ProductSummaryDTO> dtos = products.stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<Product> products = productService.getProductsByPriceRange(min, max);
        List<ProductSummaryDTO> dtos = products.stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Object> getPriceStatistics() {
        return ResponseEntity.ok(productService.getPriceStatistics());
    }

    @GetMapping("/sku/{sku}/available")
    public ResponseEntity<Boolean> isSkuAvailable(
            @PathVariable String sku,
            @RequestParam(required = false) UUID excludeId) {
        boolean available = productService.isSkuAvailable(sku, excludeId);
        return ResponseEntity.ok(available);
    }
}