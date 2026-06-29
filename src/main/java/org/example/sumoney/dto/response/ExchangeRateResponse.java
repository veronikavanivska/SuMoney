package org.example.sumoney.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.sumoney.entities.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExchangeRateResponse {
    private CurrencyCode fromCurrency;
    private CurrencyCode toCurrency;
    private BigDecimal rate;
    private LocalDate effectiveDate;
}
