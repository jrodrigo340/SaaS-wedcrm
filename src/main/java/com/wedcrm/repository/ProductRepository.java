package com.wedcrm.repository;

import com.wedcrm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Busca por SKU (único)
    Optional<Product> findBySku(String sku);

    // Busca por nome (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Produtos ativos
    List<Product> findByIsActiveTrue();

    // Produtos por categoria
    List<Product> findByCategoryIgnoreCase(String category);

    // Produtos por faixa de preço
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Busca combinada (nome, descrição, SKU)
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Product> searchProducts(@Param("search") String search);

    // Todas as categorias distintas
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    // Contagem de produtos por categoria
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();

    // Produtos mais caros
    List<Product> findTop10ByOrderByPriceDesc();

    // Produtos mais baratos
    List<Product> findTop10ByOrderByPriceAsc();

    // Verifica se SKU já existe (ignorando o próprio produto em atualização)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p " +
            "WHERE p.sku = :sku AND p.id != :id")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("id") UUID id);

    // Produtos sem descrição
    @Query("SELECT p FROM Product p WHERE p.description IS NULL OR p.description = ''")
    List<Product> findProductsWithoutDescription();

    // Estatísticas de preço
    @Query("SELECT AVG(p.price), MIN(p.price), MAX(p.price) FROM Product p WHERE p.isActive = true")
    Object[] getPriceStatistics();
}