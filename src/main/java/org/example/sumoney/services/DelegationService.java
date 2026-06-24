package org.example.sumoney.services;

import org.example.sumoney.dto.requests.CreateDelegationRequest;
import org.example.sumoney.dto.requests.DelegationFilterRequest;
import org.example.sumoney.dto.response.DelegationResponse;
import org.example.sumoney.entities.Delegation;
import org.example.sumoney.entities.User;
import org.example.sumoney.helpers.DelegationSpecification;
import org.example.sumoney.repositories.DelegationRepository;
import org.example.sumoney.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DelegationService {

    private final DelegationRepository delegationRepository;
    private final UserRepository userRepository;

    public DelegationService(
            DelegationRepository delegationRepository,
            UserRepository userRepository
    ) {
        this.delegationRepository = delegationRepository;
        this.userRepository = userRepository;
    }

    public DelegationResponse createDelegation(Long userId, CreateDelegationRequest request) {
        validateCreateDelegationRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Delegation delegation = new Delegation();
        delegation.setTitle(request.getTitle());
        delegation.setDestination(request.getDestination());
        delegation.setStartDate(request.getStartDate());
        delegation.setEndDate(request.getEndDate());
        delegation.setDescription(request.getDescription());
        delegation.setUser(user);

        Delegation savedDelegation = delegationRepository.save(delegation);

        return mapToResponse(savedDelegation);
    }

    public Page<DelegationResponse> findDelegations(
            Long userId,
            DelegationFilterRequest filter,
            Pageable pageable
    ) {
        if (filter == null) {
            filter = new DelegationFilterRequest();
        }

        validateFilter(filter);

        return delegationRepository
                .findAll(
                        DelegationSpecification.filterBy(userId, filter),
                        pageable
                )
                .map(this::mapToResponse);
    }

    public DelegationResponse getDelegationById(Long userId, Long delegationId) {
        Delegation delegation = delegationRepository.findByIdAndUser_Id(delegationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));

        return mapToResponse(delegation);
    }

    public String deleteDelegation(Long userId, Long delegationId) {
        Delegation delegation = delegationRepository.findByIdAndUser_Id(delegationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));

        delegationRepository.delete(delegation);

        return "Deleted delegation";
    }

    private void validateCreateDelegationRequest(CreateDelegationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
        }
    }

    private void validateFilter(DelegationFilterRequest filter) {
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            if (filter.getDateTo().isBefore(filter.getDateFrom())) {
                throw new IllegalArgumentException("dateTo cannot be before dateFrom");
            }
        }
    }

    private DelegationResponse mapToResponse(Delegation delegation) {
        return DelegationResponse.builder()
                .id(delegation.getId())
                .title(delegation.getTitle())
                .destination(delegation.getDestination())
                .startDate(delegation.getStartDate())
                .endDate(delegation.getEndDate())
                .description(delegation.getDescription())
                .build();
    }
}