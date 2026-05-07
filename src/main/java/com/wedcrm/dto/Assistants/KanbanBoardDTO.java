package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.util.List;

@Builder
public record KanbanBoardDTO(
        List<KanbanColumnDTO> columns
) {}
