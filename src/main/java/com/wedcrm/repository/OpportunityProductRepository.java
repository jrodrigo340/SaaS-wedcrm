package com.wedcrm.repository;

import com.wedcrm.entity.Opportunity;
import com.wedcrm.entity.OpportunityProduct;
import com.wedcrm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityProductRepository extends JpaRepository<OpportunityProduct, UUID> {

    // Busca por oportunidade
    List<OpportunityProduct> findByOpportunityId(UUID opportunityId);

    // Busca por produto
    List<OpportunityProduct> findByProductId(UUID productId);

    // Busca por oportunidade e produto
    @Query("SELECT op FROM OpportunityProduct op WHERE op.opportunity.id = :opportunityId AND op.product.id = :productId")
    OpportunityProduct findByOpportunityAndProduct(@Param("opportunityId") UUID opportunityId,
                                                   @Param("productId") UUID productId);

    // Verifica se produto já está em uma oportunidade
    @Query("SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END FROM OpportunityProduct op " +
            "WHERE op.opportunity.id = :opportunityId AND op.product.id = :productId")
    boolean existsInOpportunity(@Param("opportunityId") UUID opportunityId,
                                @Param("productId") UUID productId);

    // Soma total de produtos em uma oportunidade
    @Query("SELECT SUM(op.totalPrice) FROM OpportunityProduct op WHERE op.opportunity.id = :opportunityId")
    BigDecimal sumTotalByOpportunity(@Param("opportunityId") UUID opportunityId);

    // Soma total de quantidade de um produto em todas as oportunidades
    @Query("SELECT SUM(op.quantity) FROM OpportunityProduct op WHERE op.product.id = :productId")
    Long sumQuantityByProduct(@Param("productId") UUID productId);

    // Remove todos os produtos de uma oportunidade
    @Modifying
    @Transactional
    @Query("DELETE FROM OpportunityProduct op WHERE op.opportunity.id = :opportunityId")
    void deleteByOpportunityId(@Param("opportunityId") UUID opportunityId);

    // Produtos mais vendidos (por quantidade)
    @Query("SELECT op.product, SUM(op.quantity) as totalQuantity " +
            "FROM OpportunityProduct op " +
            "GROUP BY op.product " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();

    // Produtos mais rentáveis (por valor total)
    @Query("SELECT op.product, SUM(op.totalPrice) as totalValue " +
            "FROM OpportunityProduct op " +
            "WHERE op.opportunity.status = 'WON' " +
            "GROUP BY op.product " +
            "ORDER BY totalValue DESC")
    List<Object[]> findTopRevenueProducts();

    // Média de itens por oportunidade
    @Query("SELECT AVG(itemCount) FROM (SELECT COUNT(op) as itemCount FROM OpportunityProduct op GROUP BY op.opportunity)")
    Double averageItemsPerOpportunity();

    // Produtos com maior desconto médio
    @Query("SELECT op.product, AVG(op.discount) as avgDiscount " +
            "FROM OpportunityProduct op " +
            "GROUP BY op.product " +
            "ORDER BY avgDiscount DESC")
    List<Object[]> findProductsWithHighestAverageDiscount();

    // Estatísticas de uma oportunidade específica
    @Query("SELECT COUNT(op), SUM(op.quantity), SUM(op.totalPrice) " +
            "FROM OpportunityProduct op WHERE op.opportunity.id = :opportunityId")
    Object[] getOpportunityStats(@Param("opportunityId") UUID opportunityId);

    // Atualiza preços em massa (quando produto tem reajuste)
    @Modifying
    @Transactional
    @Query("UPDATE OpportunityProduct op SET op.unitPrice = :newPrice " +
            "WHERE op.product.id = :productId AND op.opportunity.status = 'OPEN'")
    void updatePricesForOpenOpportunities(@Param("productId") UUID productId,
                                          @Param("newPrice") BigDecimal newPrice);
}