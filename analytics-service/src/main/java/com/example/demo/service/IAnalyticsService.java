package com.example.demo.service;

import com.example.demo.dto.CategorySummaryResponse;
import com.example.demo.dto.FinancialSummaryResponse;
import com.example.demo.dto.MonthlySummaryResponse;
import com.example.demo.dto.TopExpenseResponse;

import java.util.List;

public interface IAnalyticsService {

    FinancialSummaryResponse getSummary(String userId);

    List<CategorySummaryResponse> getSummaryByCategory(String userId);

    List<MonthlySummaryResponse> getMonthlySummary(String userId);

    List<TopExpenseResponse> getTopExpenses(String userId, int limit);
}
