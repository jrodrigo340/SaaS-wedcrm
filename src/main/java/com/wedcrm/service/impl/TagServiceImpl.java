package com.wedcrm.service.impl;

import com.wedcrm.dto.request.TagRequestDTO;
import com.wedcrm.dto.response.TagResponseDTO;
import com.wedcrm.dto.dashboard.TagSummaryDTO;
import com.wedcrm.entity.Tag;
import com.wedcrm.mapper.TagMapper;
import com.wedcrm.repository.TagRepository;
import com.wedcrm.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagServiceImpl(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Override
    public List<TagSummaryDTO> getAllTags() {
        return tagRepository.findAll().stream().map(tagMapper::toSummaryDTO).collect(Collectors.toList());
    }

    @Override
    public List<TagSummaryDTO> getMostUsedTags() {
        return tagRepository.findMostUsedTags().stream().map(tagMapper::toSummaryDTO).collect(Collectors.toList());
    }

    @Override
    public TagResponseDTO createTag(TagRequestDTO request) {
        if (tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Tag com esse nome já existe");
        }
        Tag tag = tagMapper.toEntity(request);
        tag = tagRepository.save(tag);
        return tagMapper.toResponseDTO(tag);
    }

    @Override
    public TagResponseDTO getTagById(UUID id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag não encontrada"));
        return tagMapper.toResponseDTO(tag);
    }

    @Override
    public TagResponseDTO updateTag(UUID id, TagRequestDTO request) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag não encontrada"));
        if (!tag.getName().equalsIgnoreCase(request.name()) && tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Tag com esse nome já existe");
        }
        tagMapper.updateEntity(request, tag);
        tag = tagRepository.save(tag);
        return tagMapper.toResponseDTO(tag);
    }

    @Override
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag não encontrada"));
        if (!tag.getCustomers().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir tag em uso");
        }
        tagRepository.delete(tag);
    }
}
