package org.example.sumoney.repositories.projections;

import org.example.sumoney.entities.CurrencyCode;

import java.math.BigDecimal;

public interface CurrencyStatsProjection {

    CurrencyCode getCurrency();

    BigDecimal getTotalAmount();

    Long getExpensesCount();
}