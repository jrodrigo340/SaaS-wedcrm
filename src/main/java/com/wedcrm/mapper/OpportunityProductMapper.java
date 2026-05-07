package com.wedcrm.mapper;

import com.wedcrm.dto.request.OpportunityProductRequestDTO;
import com.wedcrm.dto.response.OpportunityProductResponseDTO;
import com.wedcrm.entity.OpportunityProduct;
import com.wedcrm.entity.Product;
import com.wedcrm.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.Mapping;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {BigDecimal.class})
public abstract class OpportunityProductMapper {

    @Autowired
    protected ProductRepository productRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "product", expression = "java(getProduct(request.productId()))")
    @Mapping(target = "unitPrice", expression = "java(getUnitPrice(request))")
    @Mapping(target = "discount", defaultValue = "0")
    @Mapping(target = "totalPrice", ignore = true) // Será calculado na entidade
    public abstract OpportunityProduct toEntity(OpportunityProductRequestDTO request);

    @Mapping(target = "opportunityId", source = "opportunity.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "formattedUnitPrice", expression = "java(formatPrice(opportunityProduct.getUnitPrice()))")
    @Mapping(target = "formattedDiscount", expression = "java(formatDiscount(opportunityProduct.getDiscount()))")
    @Mapping(target = "formattedTotalPrice", expression = "java(formatPrice(opportunityProduct.getTotalPrice()))")
    @Mapping(target = "subtotal", expression = "java(opportunityProduct.getSubtotal())")
    @Mapping(target = "discountAmount", expression = "java(opportunityProduct.getDiscountAmount())")
    @Mapping(target = "hasDiscount", expression = "java(opportunityProduct.hasDiscount())")
    public abstract OpportunityProductResponseDTO toResponseDTO(OpportunityProduct opportunityProduct);

    protected Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + productId));
    }

    protected BigDecimal getUnitPrice(OpportunityProductRequestDTO request) {
        if (request.unitPrice() != null) {
            return request.unitPrice();
        }
        Product product = getProduct(request.productId());
        return product.getPrice();
    }

    protected String formatPrice(BigDecimal price) {
        if (price == null) return "R$ 0,00";
        return String.format("R$ %,.2f", price);
    }

    protected String formatDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return "Sem desconto";
        }
        return String.format("%.2f%%", discount);
    }
}
