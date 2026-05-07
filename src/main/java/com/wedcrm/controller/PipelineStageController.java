package com.wedcrm.controller;

public class PipelineStageController {

    @GetMapping
    List<PipelineStageDTO> listStages()

    @PostMapping
    PipelineStageDTO createStage(PipelineStageRequest)

    @PutMapping("/{id}")
    PipelineStageDTO updateStage(UUID id, PipelineStageRequest)

    @DeleteMapping("/{id}")
    void deleteStage(UUID id)

    @PatchMapping("/reorder")
    void reorderStages(StageOrderRequest)

}
