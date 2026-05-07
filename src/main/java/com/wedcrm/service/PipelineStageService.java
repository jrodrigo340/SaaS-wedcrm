package com.wedcrm.service;

import com.wedcrm.dto.request.PipelineStageRequestDTO;
import com.wedcrm.dto.response.PipelineStageResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PipelineStageService {
    List<PipelineStageResponseDTO> getAllStages();
    PipelineStageResponseDTO createStage(PipelineStageRequestDTO request);
    PipelineStageResponseDTO getStageById(UUID id);
    PipelineStageResponseDTO updateStage(UUID id, PipelineStageRequestDTO request);
    void deleteStage(UUID id);
    void reorderStage(UUID stageId, int newOrder);
}