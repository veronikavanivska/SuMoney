package org.example.sumoney.controllers;

import org.example.sumoney.dto.response.ReceiptAnalysisResponse;
import org.example.sumoney.services.ReceiptAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/delegations/{delegationId}/receipts")
public class ReceiptAnalysisController {

    private final ReceiptAnalysisService receiptAnalysisService;

    public ReceiptAnalysisController(ReceiptAnalysisService receiptAnalysisService) {
        this.receiptAnalysisService = receiptAnalysisService;
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ReceiptAnalysisResponse> analyzeReceipt(
            @PathVariable Long delegationId,
            @RequestParam MultipartFile receiptImage,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        ReceiptAnalysisResponse response = receiptAnalysisService.analyzeReceipt(
                userId,
                delegationId,
                receiptImage
        );

        return ResponseEntity.ok(response);
    }
}