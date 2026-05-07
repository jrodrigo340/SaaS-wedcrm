package com.wedcrm.controller;

public class ReportController {

    @GetMapping("/sales")
    ReportDTO generateSalesReport(DateRange range)

    @GetMapping("/activities")
    ReportDTO generateActivityReport(DateRange range)

    @GetMapping("/customers")
    ReportDTO generateCustomerReport(DateRange range)

    @GetMapping("/export")
    void exportReport(ReportType type, Format format)

}
