package com.example.demo.controller;

import com.example.demo.dto.CategorySummaryResponse;
import com.example.demo.dto.FinancialSummaryResponse;
import com.example.demo.dto.MonthlySummaryResponse;
import com.example.demo.dto.TopExpenseResponse;
import com.example.demo.service.IAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Financial analytics endpoints")
public class AnalyticsController {

    private final IAnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial summary (total credit, debit and net balance)")
    public ResponseEntity<FinancialSummaryResponse> getSummary(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getSummary(authentication.getName()));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get expenses and income grouped by category")
    public ResponseEntity<List<CategorySummaryResponse>> getSummaryByCategory(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getSummaryByCategory(authentication.getName()));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly credit and debit totals")
    public ResponseEntity<List<MonthlySummaryResponse>> getMonthlySummary(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getMonthlySummary(authentication.getName()));
    }

    @GetMapping("/top-expenses")
    @Operation(summary = "Get top N highest expenses")
    public ResponseEntity<List<TopExpenseResponse>> getTopExpenses(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTopExpenses(authentication.getName(), limit));
    }
}
