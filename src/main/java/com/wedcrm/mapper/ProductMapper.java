package com.wedcrm.mapper;

import com.wedcrm.dto.request.ProductRequestDTO;
import com.wedcrm.dto.response.ProductResponseDTO;
import com.wedcrm.dto.dashboard.ProductSummaryDTO;
import com.wedcrm.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    @Mapping(target = "formattedPrice", source = "price", qualifiedByName = "formatPrice")
    @Mapping(target = "priceWithUnit", expression = "java(getPriceWithUnit(product))")
    @Mapping(target = "categoryIcon", expression = "java(product.getCategoryIcon())")
    ProductResponseDTO toResponseDTO(Product product);

    @Mapping(target = "formattedPrice", source = "price", qualifiedByName = "formatPrice")
    @Mapping(target = "categoryIcon", expression = "java(product.getCategoryIcon())")
    ProductSummaryDTO toSummaryDTO(Product product);

    @Named("formatPrice")
    default String formatPrice(BigDecimal price) {
        if (price == null) return "R$ 0,00";
        return String.format("R$ %,.2f", price);
    }

    default String getPriceWithUnit(Product product) {
        return product.getPriceWithUnit();
    }
}