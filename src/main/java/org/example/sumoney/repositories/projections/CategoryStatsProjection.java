package org.example.sumoney.repositories.projections;

import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;

import java.math.BigDecimal;

public interface CategoryStatsProjection {

    ExpenseCategory getCategory();

    CurrencyCode getCurrency();

    BigDecimal getTotalAmount();

    Long getExpensesCount();
}