package com.walletwise.pfm.controllers;

import com.walletwise.pfm.services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** GET /api/reports/monthly/{year}/{month} */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> monthlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(reportService.monthlyReport(userDetails.getUsername(), year, month));
    }

    /** GET /api/reports/yearly/{year} */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<Map<String, Object>> yearlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year) {
        return ResponseEntity.ok(reportService.yearlyReport(userDetails.getUsername(), year));
    }
}
