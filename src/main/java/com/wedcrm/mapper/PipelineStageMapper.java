package com.wedcrm.mapper;

import com.wedcrm.dto.request.PipelineStageRequestDTO;
import com.wedcrm.dto.response.PipelineStageResponseDTO;
import com.wedcrm.entity.PipelineStage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PipelineStageMapper {
    PipelineStage toEntity(PipelineStageRequestDTO dto);
    void updateEntity(PipelineStageRequestDTO dto, @MappingTarget PipelineStage entity);
    PipelineStageResponseDTO toResponseDTO(PipelineStage stage);
}
