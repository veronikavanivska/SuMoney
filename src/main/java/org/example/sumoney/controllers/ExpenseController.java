package org.example.sumoney.controllers;

import org.example.sumoney.dto.requests.CreateExpenseRequest;
import org.example.sumoney.dto.requests.ExpenseFilterRequest;
import org.example.sumoney.dto.response.ExpenseResponse;
import org.example.sumoney.services.ExpenseService;
import org.example.sumoney.services.FileStorageService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/delegations/{delegationId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long delegationId,
            @ModelAttribute CreateExpenseRequest request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        ExpenseResponse response = expenseService.createExpense(
                userId,
                delegationId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> findExpenses(
            @PathVariable Long delegationId,

            @ParameterObject
            ExpenseFilterRequest filter,

            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "expenseDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        Page<ExpenseResponse> response = expenseService.findExpenses(
                userId,
                delegationId,
                filter,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long delegationId,
            @PathVariable Long expenseId,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        ExpenseResponse response = expenseService.getExpenseById(
                userId,
                delegationId,
                expenseId
        );

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{expenseId}/receipt")
//    public ResponseEntity<byte[]> getReceipt(@PathVariable Long delegationId, @PathVariable Long expenseId, Principal principal) {
//        Long userId = Long.valueOf(principal.getName());
//
//        FileStorageService.StoredBytes file = expenseService.getReceiptFile(
//                userId,
//                delegationId,
//                expenseId
//        );
//
//        return ResponseEntity.ok()
//                .header("Content-Disposition", "inline")
//                .contentType(org.springframework.http.MediaType.parseMediaType(file.contentType()))
//                .body(file.bytes());
//    }
@GetMapping("/{expenseId}/receipt-url")
public ResponseEntity<String> getReceiptUrl(
        @PathVariable Long delegationId,
        @PathVariable Long expenseId,
        Principal principal
) {
    Long userId = Long.valueOf(principal.getName());

    String url = expenseService.getReceiptUrl(
            userId,
            delegationId,
            expenseId
    );

    return ResponseEntity.ok(url);
}
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpense(
            @PathVariable Long delegationId,
            @PathVariable Long expenseId,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        String response = expenseService.deleteExpense(
                userId,
                delegationId,
                expenseId
        );

        return ResponseEntity.ok(response);
    }
}