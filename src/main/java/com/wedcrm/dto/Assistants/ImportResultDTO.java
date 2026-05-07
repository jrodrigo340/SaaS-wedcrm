package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record ImportResultDTO(
        int successCount,
        List<ImportError> errors
) {
    public ImportResultDTO() {
        this(0, new ArrayList<>());
    }

    public void incrementSuccess() {
        // Implementação
    }

    public void addError(long line, String message) {
        // Implementação
    }
}

record ImportError(long line, String message) {}