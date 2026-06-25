package org.example.sumoney.dto.requests;

import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseFilterRequest {

    private String contains;

    private ExpenseCategory category;

    private CurrencyCode currency;

    private BigDecimal amountFrom;

    private BigDecimal amountTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
}
