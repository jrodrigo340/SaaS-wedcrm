package com.wedcrm.service.impl;

import com.wedcrm.dto.request.PipelineStageRequestDTO;
import com.wedcrm.dto.response.PipelineStageResponseDTO;
import com.wedcrm.entity.PipelineStage;
import com.wedcrm.mapper.PipelineStageMapper;
import com.wedcrm.repository.PipelineStageRepository;
import com.wedcrm.service.PipelineStageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PipelineStageServiceImpl implements PipelineStageService {

    private final PipelineStageRepository stageRepository;
    private final PipelineStageMapper stageMapper;

    public PipelineStageServiceImpl(PipelineStageRepository stageRepository, PipelineStageMapper stageMapper) {
        this.stageRepository = stageRepository;
        this.stageMapper = stageMapper;
    }

    @Override
    public List<PipelineStageResponseDTO> getAllStages() {
        return stageRepository.findAllByOrderByOrderAsc()
                .stream()
                .map(stageMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PipelineStageResponseDTO createStage(PipelineStageRequestDTO request) {
        // Se já existe estágio com a ordem solicitada, desloca os posteriores
        if (stageRepository.existsByOrder(request.order())) {
            List<PipelineStage> stages = stageRepository.findAllByOrderByOrderAsc();
            for (PipelineStage s : stages) {
                if (s.getOrder() >= request.order()) {
                    s.setOrder(s.getOrder() + 1);
                    stageRepository.save(s);
                }
            }
        }
        PipelineStage stage = stageMapper.toEntity(request);
        stage = stageRepository.save(stage);
        return stageMapper.toResponseDTO(stage);
    }

    @Override
    public PipelineStageResponseDTO getStageById(UUID id) {
        PipelineStage stage = stageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));
        return stageMapper.toResponseDTO(stage);
    }

    @Override
    public PipelineStageResponseDTO updateStage(UUID id, PipelineStageRequestDTO request) {
        PipelineStage stage = stageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));
        stageMapper.updateEntity(request, stage);
        stage = stageRepository.save(stage);
        return stageMapper.toResponseDTO(stage);
    }

    @Override
    public void deleteStage(UUID id) {
        PipelineStage stage = stageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));
        if (!stage.getOpportunities().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir estágio com oportunidades associadas");
        }
        int deletedOrder = stage.getOrder();
        stageRepository.delete(stage);
        // Reordenar os estágios posteriores
        List<PipelineStage> stages = stageRepository.findAllByOrderByOrderAsc();
        for (PipelineStage s : stages) {
            if (s.getOrder() > deletedOrder) {
                s.setOrder(s.getOrder() - 1);
                stageRepository.save(s);
            }
        }
    }

    @Override
    public void reorderStage(UUID stageId, int newOrder) {
        PipelineStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Estágio não encontrado"));
        int oldOrder = stage.getOrder();
        if (oldOrder == newOrder) return;

        List<PipelineStage> allStages = stageRepository.findAllByOrderByOrderAsc();
        if (newOrder < 1) newOrder = 1;
        if (newOrder > allStages.size()) newOrder = allStages.size();

        // Remove e reinsere na nova posição
        allStages.remove(stage);
        allStages.add(newOrder - 1, stage);

        // Reatribui ordens sequenciais
        for (int i = 0; i < allStages.size(); i++) {
            allStages.get(i).setOrder(i + 1);
        }
        stageRepository.saveAll(allStages);
    }
}