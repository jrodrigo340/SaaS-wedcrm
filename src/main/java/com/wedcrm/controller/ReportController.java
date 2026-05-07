package com.wedcrm.controller;

import com.wedcrm.dto.Assistants.DateRangeDTO;
import com.wedcrm.dto.response.ReportResponseDTO;
import com.wedcrm.enums.ReportType;
import com.wedcrm.service.ReportService;
import com.wedcrm.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Relatórios", description = "Geração de relatórios gerenciais")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/sales")
    @Operation(summary = "Relatório de vendas (PDF)")
    public ResponseEntity<InputStreamResource> generateSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        InputStreamResource resource = reportService.generateSalesReportPdf(startDate, endDate, null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/activities")
    @Operation(summary = "Relatório de atividades (XLSX)")
    public ResponseEntity<InputStreamResource> generateActivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        InputStreamResource resource = reportService.generateActivitiesReportXlsx(startDate, endDate, null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activities_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/customers")
    @Operation(summary = "Relatório de clientes (XLSX)")
    public ResponseEntity<InputStreamResource> generateCustomerReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignedToId) {
        InputStreamResource resource = reportService.generateCustomersReportXlsx(status, assignedToId != null ? java.util.UUID.fromString(assignedToId) : null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar relatório genérico", description = "Exporta relatório em formato PDF ou XLSX")
    public ResponseEntity<InputStreamResource> exportReport(
            @RequestParam ReportType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "XLSX") String format) {
        InputStreamResource resource = reportService.exportReport(type, startDate, endDate, format);
        String filename = type.name().toLowerCase() + "_report." + format.toLowerCase();
        MediaType mediaType = "PDF".equalsIgnoreCase(format) ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(resource);
    }
}