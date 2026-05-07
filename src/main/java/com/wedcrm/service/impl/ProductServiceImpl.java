package com.wedcrm.service.impl;

import com.wedcrm.dto.request.ProductRequestDTO;
import com.wedcrm.dto.response.ProductResponseDTO;
import com.wedcrm.dto.dashboard.ProductSummaryDTO;
import com.wedcrm.entity.Product;
import com.wedcrm.mapper.ProductMapper;
import com.wedcrm.repository.ProductRepository;
import com.wedcrm.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Page<ProductSummaryDTO> listProducts(Boolean activeOnly, Pageable pageable) {
        Page<Product> products;
        if (Boolean.TRUE.equals(activeOnly)) {
            products = productRepository.findByIsActiveTrue(pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        return products.map(productMapper::toSummaryDTO);
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        if (request.sku() != null && productRepository.findBySku(request.sku()).isPresent()) {
            throw new IllegalArgumentException("SKU já existe");
        }
        Product product = productMapper.toEntity(request);
        product.setIsActive(true);
        product = productRepository.save(product);
        return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(UUID id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        if (request.sku() != null && !request.sku().equals(product.getSku()) &&
                productRepository.findBySku(request.sku()).isPresent()) {
            throw new IllegalArgumentException("SKU já existe");
        }
        productMapper.updateEntity(request, product);
        product = productRepository.save(product);
        return productMapper.toResponseDTO(product);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    public List<ProductSummaryDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max)
                .stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
}
