package org.example.sumoney.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReceiptAnalysisResponse {
    private String title;
    private BigDecimal amount;
    private CurrencyCode currency;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private String note;
    private String confidence;
    private List<String> warnings;
}