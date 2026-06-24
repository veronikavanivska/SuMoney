package org.example.sumoney.controllers;

import org.example.sumoney.dto.requests.CreateDelegationRequest;
import org.example.sumoney.dto.requests.DelegationFilterRequest;
import org.example.sumoney.dto.response.DelegationResponse;
import org.example.sumoney.services.DelegationService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/delegations")
public class DelegationController {

    private final DelegationService delegationService;

    public DelegationController(DelegationService delegationService) {
        this.delegationService = delegationService;
    }

    @PostMapping
    public ResponseEntity<DelegationResponse> createDelegation(
            @RequestBody CreateDelegationRequest request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        DelegationResponse response = delegationService.createDelegation(userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DelegationResponse>> findDelegations(
            @ParameterObject DelegationFilterRequest filter,

            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "startDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        Page<DelegationResponse> response = delegationService.findDelegations(
                userId,
                filter,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{delegationId}")
    public ResponseEntity<DelegationResponse> getDelegationById(
            @PathVariable Long delegationId,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        DelegationResponse response = delegationService.getDelegationById(
                userId,
                delegationId
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{delegationId}")
    public ResponseEntity<String> deleteDelegation(
            @PathVariable Long delegationId,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        String response = delegationService.deleteDelegation(
                userId,
                delegationId
        );

        return ResponseEntity.ok(response);
    }
}