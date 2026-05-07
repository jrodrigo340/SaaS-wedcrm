package com.wedcrm.service;

import org.springframework.core.io.InputStreamResource;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReportService {
    InputStreamResource generateSalesReportPdf(LocalDateTime startDate, LocalDateTime endDate, UUID userId);
    InputStreamResource generateSalesReportXlsx(LocalDateTime startDate, LocalDateTime endDate, UUID userId);
    InputStreamResource generateCustomersReportXlsx(String status, UUID assignedToId);
    InputStreamResource generateActivitiesReportXlsx(LocalDateTime startDate, LocalDateTime endDate, UUID assignedToId);
}
