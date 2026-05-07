package com.wedcrm.controller;

import com.wedcrm.dto.Assistants.ActivityDTO;
import com.wedcrm.service.ActivityService;
import com.wedcrm.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> listActivities(
            ActivityFilter filter,
            Pageable pageable) {

        Page<ActivityDTO> activities =
                activityService.listActivities(filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(activities)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityDTO>> createActivity(
            @RequestBody ActivityRequest request) {

        ActivityDTO activity =
                activityService.createActivity(request);

        return ResponseEntity.ok(
                ApiResponse.success(activity)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityDTO>> getActivityById(
            @PathVariable UUID id) {

        ActivityDTO activity =
                activityService.getActivityById(id);

        return ResponseEntity.ok(
                ApiResponse.success(activity)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityDTO>> updateActivity(
            @PathVariable UUID id,
            @RequestBody ActivityRequest request) {

        ActivityDTO activity =
                activityService.updateActivity(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(activity)
        );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeActivity(
            @PathVariable UUID id) {

        activityService.completeActivity(id);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelActivity(
            @PathVariable UUID id,
            @RequestParam String reason) {

        activityService.cancelActivity(id, reason);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getTodayActivities(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<ActivityDTO> activities =
                activityService.getTodayActivities(userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(activities)
        );
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getOverdueActivities(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<ActivityDTO> activities =
                activityService.getOverdueActivities(userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(activities)
        );
    }

    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getActivityCalendar(
            @PathVariable int year,
            @PathVariable int month,
            Authentication authentication) {

        String userEmail = authentication.getName();

        YearMonth yearMonth = YearMonth.of(year, month);

        List<ActivityDTO> activities =
                activityService.getActivityCalendar(userEmail, yearMonth);

        return ResponseEntity.ok(
                ApiResponse.success(activities)
        );
    }
}