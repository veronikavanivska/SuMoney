package org.example.sumoney.dto.response;


import lombok.Builder;
import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;

import java.math.BigDecimal;

@Data
@Builder
public class CurrencyStatsResponse {

    private CurrencyCode currency;

    private BigDecimal totalAmount;

    private Long expensesCount;
}