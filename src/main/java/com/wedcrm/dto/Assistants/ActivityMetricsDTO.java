package com.wedcrm.dto.Assistants;

import lombok.Builder;

import java.util.List;

@Builder
public record ActivityMetricsDTO(
        long totalActivities,
        long completedActivities,
        long pendingActivities,
        long overdueActivities,
        double overallCompletionRate,
        List<ActivityTypeMetricDTO> byType,
        List<PriorityMetricDTO> byPriority
) {}