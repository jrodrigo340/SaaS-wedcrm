package com.wedcrm.service.impl;

import com.wedcrm.entity.Customer;
import com.wedcrm.entity.Opportunity;
import com.wedcrm.entity.User;
import com.wedcrm.enums.CustomerStatus;
import com.wedcrm.enums.OpportunityStatus;
import com.wedcrm.repository.*;
import com.wedcrm.service.ReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(OpportunityRepository opportunityRepository,
                             CustomerRepository customerRepository,
                             ActivityRepository activityRepository,
                             UserRepository userRepository) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public InputStreamResource generateSalesReportPdf(LocalDateTime startDate, LocalDateTime endDate, UUID userId) {
        // Placeholder: gerar PDF com alguma biblioteca (OpenPDF, iText)
        throw new UnsupportedOperationException("Geração de PDF não implementada ainda");
    }

    @Override
    public InputStreamResource generateSalesReportXlsx(LocalDateTime startDate, LocalDateTime endDate, UUID userId) {
        List<Opportunity> opportunities;
        if (userId != null) {
            User user = userRepository.findById(userId).orElseThrow();
            opportunities = opportunityRepository.findByAssignedToAndClosedAtBetween(user, startDate, endDate);
        } else {
            opportunities = opportunityRepository.findByClosedAtBetween(startDate, endDate);
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vendas");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Título", "Cliente", "Valor", "Status", "Data Fechamento", "Vendedor"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            int rowIdx = 1;
            for (Opportunity opp : opportunities) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(opp.getId().toString());
                row.createCell(1).setCellValue(opp.getTitle());
                row.createCell(2).setCellValue(opp.getCustomer().getFullName());
                row.createCell(3).setCellValue(opp.getValue() != null ? opp.getValue().doubleValue() : 0);
                row.createCell(4).setCellValue(opp.getStatus().name());
                row.createCell(5).setCellValue(opp.getClosedAt() != null ? opp.getClosedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                row.createCell(6).setCellValue(opp.getAssignedTo() != null ? opp.getAssignedTo().getName() : "");
            }
            workbook.write(out);
            return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório XLSX", e);
        }
    }

    @Override
    public InputStreamResource generateCustomersReportXlsx(String status, UUID assignedToId) {
        List<Customer> customers;
        if (status != null) {
            CustomerStatus customerStatus = CustomerStatus.valueOf(status);
            if (assignedToId != null) {
                User user = userRepository.findById(assignedToId).orElseThrow();
                customers = customerRepository.findByAssignedToAndStatus(user, customerStatus);
            } else {
                customers = customerRepository.findByStatus(customerStatus);
            }
        } else if (assignedToId != null) {
            User user = userRepository.findById(assignedToId).orElseThrow();
            customers = customerRepository.findByAssignedTo(user);
        } else {
            customers = customerRepository.findAll();
        }
        // geração do Excel (similar ao anterior)
        // ... (omitido por brevidade)
        throw new UnsupportedOperationException("Relatório de clientes será implementado em breve");
    }

    @Override
    public InputStreamResource generateActivitiesReportXlsx(LocalDateTime startDate, LocalDateTime endDate, UUID assignedToId) {
        // implementação similar
        throw new UnsupportedOperationException("Relatório de atividades será implementado em breve");
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
