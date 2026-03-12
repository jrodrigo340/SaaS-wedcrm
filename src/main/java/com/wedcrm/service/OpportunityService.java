package com.wedcrm.service;

import com.wedcrm.dto.opportunity.*;
import com.wedcrm.entity.Opportunity;

import java.util.List;
import java.util.UUID;

public interface OpportunityService {

    Opportunity createOpportunity(OpportunityRequest request);

    Opportunity updateOpportunity(UUID id, OpportunityRequest request);

    void moveStage(UUID id, UUID newStageId);

    void closeAsWon(UUID id, CloseRequest request);

    void closeAsLost(UUID id, LostRequest request);

    void addProduct(UUID oppId, ProductRequest request);

    void removeProduct(UUID oppId, UUID productId);

    PipelineKanbanResponse getPipelineKanban(UUID userId);

    PipelineMetricsResponse getPipelineMetrics(DateRange range);

    List<Opportunity> getOverdueOpportunities(UUID userId);
}
