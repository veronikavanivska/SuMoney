package org.example.sumoney.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseResponse {

    private Long id;

    private String title;

    private BigDecimal amount;

    private CurrencyCode currency;

    private ExpenseCategory category;

    private LocalDate expenseDate;

    private String note;

    private boolean hasReceipt;

    private String receiptContentType;

    private Long delegationId;
}