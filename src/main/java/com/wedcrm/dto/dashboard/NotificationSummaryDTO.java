package com.wedcrm.dto.dashboard;

import com.wedcrm.dto.response.NotificationResponseDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record NotificationSummaryDTO(
        long unreadCount,
        long totalCount,
        boolean hasUnread,
        List<NotificationResponseDTO> recentUnread
) {}