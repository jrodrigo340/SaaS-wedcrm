package com.wedcrm.mapper;

import com.wedcrm.dto.request.TagRequestDTO;
import com.wedcrm.dto.response.TagResponseDTO;
import com.wedcrm.dto.dashboard.TagSummaryDTO;
import com.wedcrm.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", constant = "true")
    Tag toEntity(TagRequestDTO dto);

    @Mapping(target = "validColor", source = "tag", qualifiedByName = "getValidColor")
    @Mapping(target = "customerCount", expression = "java(tag.getCustomerCount())")
    @Mapping(target = "isUsed", expression = "java(tag.isUsed())")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "createdBy", source = "createdBy")
    TagResponseDTO toResponseDTO(Tag tag);

    @Mapping(target = "validColor", source = "tag", qualifiedByName = "getValidColor")
    @Mapping(target = "customerCount", expression = "java(tag.getCustomerCount())")
    TagSummaryDTO toSummaryDTO(Tag tag);

    @Named("getValidColor")
    default String getValidColor(Tag tag) {
        return tag.getValidColor();
    }

    @Named("formatDateTime")
    default String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}