package com.wedcrm.service;

import com.wedcrm.dto.*;
import com.wedcrm.dto.Assistants.DateRangeDTO;
import com.wedcrm.dto.Assistants.KanbanBoardDTO;
import com.wedcrm.dto.Assistants.PipelineMetricsDTO;
import com.wedcrm.dto.filter.OpportunityFilterDTO;
import com.wedcrm.dto.request.CloseRequestDTO;
import com.wedcrm.dto.request.LostRequestDTO;
import com.wedcrm.dto.request.OpportunityProductRequestDTO;
import com.wedcrm.dto.request.OpportunityRequestDTO;
import com.wedcrm.dto.response.OpportunityProductResponseDTO;
import com.wedcrm.dto.response.OpportunityResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OpportunityService {

    OpportunityResponseDTO createOpportunity(OpportunityRequestDTO request);

    OpportunityResponseDTO updateOpportunity(UUID id, OpportunityRequestDTO request);

    OpportunityResponseDTO getOpportunityById(UUID id);

    Page<OpportunityResponseDTO> listOpportunities(OpportunityFilterDTO filter, Pageable pageable);

    void deleteOpportunity(UUID id);

    OpportunityResponseDTO moveStage(UUID id, UUID newStageId);

    OpportunityResponseDTO closeAsWon(UUID id, CloseRequestDTO request);

    OpportunityResponseDTO closeAsLost(UUID id, LostRequestDTO request);

    OpportunityProductResponseDTO addProduct(UUID oppId, OpportunityProductRequestDTO request);

    void removeProduct(UUID oppId, UUID productId);

    KanbanBoardDTO getPipelineKanban(UUID userId);

    PipelineMetricsDTO getPipelineMetrics(DateRangeDTO dateRange);

    List<OpportunityResponseDTO> getOverdueOpportunities(UUID userId);
}