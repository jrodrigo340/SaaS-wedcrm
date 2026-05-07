package com.wedcrm.service;

import com.wedcrm.dto.request.TagRequestDTO;
import com.wedcrm.dto.response.TagResponseDTO;
import com.wedcrm.dto.dashboard.TagSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface TagService {
    List<TagSummaryDTO> getAllTags();
    List<TagSummaryDTO> getMostUsedTags();
    TagResponseDTO createTag(TagRequestDTO request);
    TagResponseDTO getTagById(UUID id);
    TagResponseDTO updateTag(UUID id, TagRequestDTO request);
    void deleteTag(UUID id);
}