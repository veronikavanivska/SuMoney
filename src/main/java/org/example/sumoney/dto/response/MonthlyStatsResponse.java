package org.example.sumoney.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MonthlyStatsResponse {


    private Integer year;

    private Integer month;

    private Long expensesCount;

    private List<CurrencyStatsResponse> totalsByCurrency;

    private List<CategoryStatsResponse> categories;
}