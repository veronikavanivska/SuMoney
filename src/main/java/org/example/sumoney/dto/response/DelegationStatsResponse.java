package org.example.sumoney.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DelegationStatsResponse {

    private Long delegationId;

    private String title;

    private String destination;

    private Long expensesCount;

    private List<CurrencyStatsResponse> totalsByCurrency;

    private List<CategoryStatsResponse> categories;
}