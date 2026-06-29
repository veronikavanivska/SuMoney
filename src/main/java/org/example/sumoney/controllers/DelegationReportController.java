package org.example.sumoney.controllers;

import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.services.DelegationReportExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/delegations/{delegationId}/reports")
public class DelegationReportController {

    private final DelegationReportExcelService delegationReportExcelService;

    public DelegationReportController(
            DelegationReportExcelService delegationReportExcelService
    ) {
        this.delegationReportExcelService = delegationReportExcelService;
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcelReport(
            @PathVariable Long delegationId,
            @RequestParam(defaultValue = "PLN") CurrencyCode targetCurrency,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        byte[] file = delegationReportExcelService.generateDelegationExpenseClaim(
                userId,
                delegationId,
                targetCurrency
        );

        String filename = "delegation-" + delegationId
                + "-expense-claim-" + targetCurrency + ".xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(file);
    }
}