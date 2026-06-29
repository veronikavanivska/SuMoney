package org.example.sumoney.services;

import org.example.sumoney.dto.response.ExchangeRateResponse;
import org.example.sumoney.entities.CurrencyCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExchangeRateService {

    private final RestClient restClient;

    public ExchangeRateService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.nbp.pl/api")
                .build();
    }


    public ExchangeRateResponse getRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now();
        }

        if (fromCurrency == toCurrency) {
            return ExchangeRateResponse.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .rate(BigDecimal.ONE)
                    .effectiveDate(date)
                    .build();
        }

        BigDecimal fromToPln = getRateToPln(fromCurrency, date);
        BigDecimal toToPln = getRateToPln(toCurrency, date);

        BigDecimal rate = fromToPln.divide(
                toToPln,
                8,
                RoundingMode.HALF_UP
        );

        return ExchangeRateResponse.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(rate)
                .effectiveDate(date)
                .build();
    }

    public BigDecimal convert(
            BigDecimal amount,
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        ExchangeRateResponse rate = getRate(fromCurrency, toCurrency, date);

        return amount.multiply(rate.getRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRateToPln(CurrencyCode currencyCode, LocalDate date) {
        if (currencyCode == CurrencyCode.PLN) {
            return BigDecimal.ONE;
        }

        LocalDate currentDate = date;

        for (int i = 0; i < 7; i++) {
            try {
                NbpRateResponse response = restClient.get()
                        .uri("/exchangerates/rates/a/{code}/{date}/?format=json",
                                currencyCode.name(),
                                currentDate
                        )
                        .retrieve()
                        .body(NbpRateResponse.class);

                if (response == null ||
                        response.rates() == null ||
                        response.rates().isEmpty()) {
                    throw new IllegalArgumentException("Exchange rate not found");
                }

                return response.rates().get(0).mid();

            } catch (Exception e) {
                currentDate = currentDate.minusDays(1);
            }
        }

        throw new IllegalArgumentException(
                "Exchange rate not found for currency: " + currencyCode
        );
    }

    public List<CurrencyCode> getSupportedCurrencies() {
        return List.of(CurrencyCode.values());
    }

    public record NbpRateResponse(
            String table,
            String currency,
            String code,
            List<NbpRate> rates
    ) {
    }

    public record NbpRate(
            String no,
            LocalDate effectiveDate,
            BigDecimal mid
    ) {
    }
}