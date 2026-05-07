package com.wedcrm.repository;

import com.wedcrm.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    // Busca por nome (case insensitive)
    Optional<Tag> findByNameIgnoreCase(String name);

    // Busca tags por nome contendo (case insensitive)
    List<Tag> findByNameContainingIgnoreCase(String name);

    // Verifica se nome já existe
    boolean existsByNameIgnoreCase(String name);

    // Lista tags mais usadas
    @Query("SELECT t FROM Tag t ORDER BY SIZE(t.customers) DESC")
    List<Tag> findMostUsedTags();

    // Tags não utilizadas
    @Query("SELECT t FROM Tag t WHERE SIZE(t.customers) = 0")
    List<Tag> findUnusedTags();

    // Busca tags por nome exato (case insensitive)
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) = LOWER(:name)")
    Optional<Tag> findByNameExact(@Param("name") String name);

    // Tags com cor personalizada
    @Query("SELECT t FROM Tag t WHERE t.color IS NOT NULL AND t.color != '#6c757d'")
    List<Tag> findTagsWithCustomColor();

    // Tags sem descrição
    @Query("SELECT t FROM Tag t WHERE t.description IS NULL OR t.description = ''")
    List<Tag> findTagsWithoutDescription();

    // Conta clientes por tag
    @Query("SELECT t.name, SIZE(t.customers) FROM Tag t")
    List<Object[]> countCustomersByTag();

    // Tags por cor
    List<Tag> findByColor(String color);
}