package org.example.sumoney.controllers;

import org.example.sumoney.dto.response.DelegationStatsResponse;
import org.example.sumoney.dto.response.MonthlyStatsResponse;
import org.example.sumoney.services.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/delegations/{delegationId}")
    public ResponseEntity<DelegationStatsResponse> getDelegationStats(
            @PathVariable Long delegationId,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        return ResponseEntity.ok(
                statisticsService.getDelegationStats(userId, delegationId)
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStatsResponse>> getMonthlyStats(
            @RequestParam Integer year,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        return ResponseEntity.ok(
                statisticsService.getMonthlyStats(userId, year)
        );
    }
}