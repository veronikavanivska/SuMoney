package org.example.sumoney.dto.requests;

import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {

    private String title;

    private BigDecimal amount;

    private CurrencyCode currency;

    private ExpenseCategory category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate;

    private String note;

    private MultipartFile receiptImage;
}