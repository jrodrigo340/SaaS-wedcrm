package com.wedcrm.repository;

import com.wedcrm.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {

    // Busca todos os estágios ordenados pela posição
    List<PipelineStage> findAllByOrderByOrderAsc();

    // Busca estágios finais (ganho ou perda)
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.isWon = true OR ps.isLost = true ORDER BY ps.order")
    List<PipelineStage> findFinalStages();

    // Busca estágios de progresso (não finais)
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.isWon = false AND ps.isLost = false ORDER BY ps.order")
    List<PipelineStage> findProgressStages();

    // Busca estágio de ganho
    Optional<PipelineStage> findByIsWonTrue();

    // Busca estágio de perda
    Optional<PipelineStage> findByIsLostTrue();

    // Verifica se existe estágio com a mesma ordem
    boolean existsByOrder(Integer order);

    // Reordena os estágios (útil quando um é removido)
    @Modifying
    @Transactional
    @Query("UPDATE PipelineStage ps SET ps.order = ps.order - 1 WHERE ps.order > :deletedOrder")
    void reorderAfterDeletion(@Param("deletedOrder") Integer deletedOrder);

    // Conta quantos estágios existem
    long count();

    // Busca o próximo estágio na ordem
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.order > :currentOrder ORDER BY ps.order ASC LIMIT 1")
    Optional<PipelineStage> findNextStage(@Param("currentOrder") Integer currentOrder);

    // Busca o estágio anterior na ordem
    @Query("SELECT ps FROM PipelineStage ps WHERE ps.order < :currentOrder ORDER BY ps.order DESC LIMIT 1")
    Optional<PipelineStage> findPreviousStage(@Param("currentOrder") Integer currentOrder);
}