package org.example.sumoney.controllers;

import org.example.sumoney.dto.response.ExchangeRateResponse;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.services.ExchangeRateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<ExchangeRateResponse> getRate(
            @RequestParam CurrencyCode fromCurrency,
            @RequestParam CurrencyCode toCurrency,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        ExchangeRateResponse response = exchangeRateService.getRate(
                fromCurrency,
                toCurrency,
                date
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam BigDecimal amount,
            @RequestParam CurrencyCode fromCurrency,
            @RequestParam CurrencyCode toCurrency,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        BigDecimal convertedAmount = exchangeRateService.convert(
                amount,
                fromCurrency,
                toCurrency,
                date
        );

        ExchangeRateResponse rate = exchangeRateService.getRate(
                fromCurrency,
                toCurrency,
                date
        );

        return ResponseEntity.ok(Map.of(
                "originalAmount", amount,
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "rate", rate.getRate(),
                "effectiveDate", rate.getEffectiveDate(),
                "convertedAmount", convertedAmount
        ));
    }

    @GetMapping("/currencies")
    public ResponseEntity<List<CurrencyCode>> getSupportedCurrencies() {
        return ResponseEntity.ok(exchangeRateService.getSupportedCurrencies());
    }
}