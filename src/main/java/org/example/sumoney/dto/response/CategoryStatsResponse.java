package org.example.sumoney.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryStatsResponse {

    private ExpenseCategory category;

    private CurrencyCode currency;

    private BigDecimal totalAmount;

    private Long expensesCount;
}