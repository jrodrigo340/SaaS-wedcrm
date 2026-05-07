package com.wedcrm.service;

import com.wedcrm.dto.request.ProductRequestDTO;
import com.wedcrm.dto.response.ProductResponseDTO;
import com.wedcrm.dto.dashboard.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    Page<ProductSummaryDTO> listProducts(Boolean activeOnly, Pageable pageable);
    ProductResponseDTO createProduct(ProductRequestDTO request);
    ProductResponseDTO getProductById(UUID id);
    ProductResponseDTO updateProduct(UUID id, ProductRequestDTO request);
    void deleteProduct(UUID id);
    List<String> getAllCategories();
    List<ProductSummaryDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max);
}