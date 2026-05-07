package com.wedcrm.controller;

import com.wedcrm.dto.request.TagRequestDTO;
import com.wedcrm.dto.response.TagResponseDTO;
import com.wedcrm.dto.dashboard.TagSummaryDTO;
import com.wedcrm.entity.Tag;
import com.wedcrm.mapper.TagMapper;
import com.wedcrm.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagSummaryDTO>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagSummaryDTO> dtos = tags.stream()
                .map(tagMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/most-used")
    public ResponseEntity<List<TagSummaryDTO>> getMostUsedTags() {
        List<Tag> tags = tagService.getMostUsedTags();
        List<TagSummaryDTO> dtos = tags.stream()
                .map(tagMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unused")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<TagSummaryDTO>> getUnusedTags() {
        List<Tag> tags = tagService.getUnusedTags();
        List<TagSummaryDTO> dtos = tags.stream()
                .map(tagMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> getTagById(@PathVariable UUID id) {
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(tagMapper.toResponseDTO(tag));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagResponseDTO> getTagByName(@PathVariable String name) {
        Tag tag = tagService.getTagByName(name);
        return ResponseEntity.ok(tagMapper.toResponseDTO(tag));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagSummaryDTO>> searchTags(@RequestParam String q) {
        List<Tag> tags = tagService.searchTags(q);
        List<TagSummaryDTO> dtos = tags.stream()
                .map(tagMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<TagResponseDTO> createTag(@Valid @RequestBody TagRequestDTO request) {
        Tag tag = tagMapper.toEntity(request);
        Tag created = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tagMapper.toResponseDTO(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<TagResponseDTO> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagRequestDTO request) {
        Tag tag = tagMapper.toEntity(request);
        Tag updated = tagService.updateTag(id, tag);
        return ResponseEntity.ok(tagMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Object[]>> getTagStatistics() {
        return ResponseEntity.ok(tagService.getTagStatistics());
    }

    @GetMapping("/available")
    public ResponseEntity<Boolean> isTagNameAvailable(
            @RequestParam String name,
            @RequestParam(required = false) UUID excludeId) {
        boolean available = tagService.isTagNameAvailable(name, excludeId);
        return ResponseEntity.ok(available);
    }
}