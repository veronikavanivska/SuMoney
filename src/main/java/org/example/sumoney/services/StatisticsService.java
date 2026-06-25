package org.example.sumoney.services;

import org.example.sumoney.dto.response.CategoryStatsResponse;
import org.example.sumoney.dto.response.CurrencyStatsResponse;
import org.example.sumoney.dto.response.DelegationStatsResponse;
import org.example.sumoney.dto.response.MonthlyStatsResponse;
import org.example.sumoney.entities.Delegation;
import org.example.sumoney.repositories.DelegationRepository;
import org.example.sumoney.repositories.ExpenseRepository;
import org.example.sumoney.repositories.projections.MonthlyCategoryStatsProjection;
import org.example.sumoney.repositories.projections.MonthlyCurrencyStatsProjection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    private final ExpenseRepository expenseRepository;
    private final DelegationRepository delegationRepository;

    public StatisticsService(
            ExpenseRepository expenseRepository,
            DelegationRepository delegationRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.delegationRepository = delegationRepository;
    }

    public DelegationStatsResponse getDelegationStats(
            Long userId,
            Long delegationId
    ) {
        Delegation delegation = delegationRepository.findByIdAndUser_Id(delegationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));

        List<CurrencyStatsResponse> totalsByCurrency = expenseRepository
                .getCurrencyStatsByDelegation(userId, delegationId)
                .stream()
                .map(item -> CurrencyStatsResponse.builder()
                        .currency(item.getCurrency())
                        .totalAmount(item.getTotalAmount())
                        .expensesCount(item.getExpensesCount())
                        .build())
                .toList();

        Long expensesCount = totalsByCurrency.stream()
                .mapToLong(CurrencyStatsResponse::getExpensesCount)
                .sum();

        List<CategoryStatsResponse> categories = expenseRepository
                .getCategoryStatsByDelegation(userId, delegationId)
                .stream()
                .map(item -> CategoryStatsResponse.builder()
                        .category(item.getCategory())
                        .currency(item.getCurrency())
                        .totalAmount(item.getTotalAmount())
                        .expensesCount(item.getExpensesCount())
                        .build())
                .toList();

        return DelegationStatsResponse.builder()
                .delegationId(delegation.getId())
                .title(delegation.getTitle())
                .destination(delegation.getDestination())
                .expensesCount(expensesCount)
                .totalsByCurrency(totalsByCurrency)
                .categories(categories)
                .build();
    }

    public List<MonthlyStatsResponse> getMonthlyStats(
            Long userId,
            Integer year
    ) {
        if (year == null) {
            throw new IllegalArgumentException("Year is required");
        }

        List<MonthlyCurrencyStatsProjection> currencyStats =
                expenseRepository.getMonthlyCurrencyStats(userId, year);

        List<MonthlyCategoryStatsProjection> categoryStats =
                expenseRepository.getMonthlyCategoryStats(userId, year);

        List<MonthlyStatsResponse> response = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            int currentMonth = month;

            List<CurrencyStatsResponse> totalsByCurrency = currencyStats.stream()
                    .filter(item -> item.getExpenseMonth().equals(currentMonth))
                    .map(item -> CurrencyStatsResponse.builder()
                            .currency(item.getCurrency())
                            .totalAmount(item.getTotalAmount())
                            .expensesCount(item.getExpensesCount())
                            .build())
                    .toList();

            List<CategoryStatsResponse> categories = categoryStats.stream()
                    .filter(item -> item.getExpenseMonth().equals(currentMonth))
                    .map(item -> CategoryStatsResponse.builder()
                            .category(item.getCategory())
                            .currency(item.getCurrency())
                            .totalAmount(item.getTotalAmount())
                            .expensesCount(item.getExpensesCount())
                            .build())
                    .toList();

            Long expensesCount = totalsByCurrency.stream()
                    .mapToLong(CurrencyStatsResponse::getExpensesCount)
                    .sum();

            response.add(
                    MonthlyStatsResponse.builder()
                            .year(year)
                            .month(month)
                            .expensesCount(expensesCount)
                            .totalsByCurrency(totalsByCurrency)
                            .categories(categories)
                            .build()
            );
        }

        return response;
    }
}