package org.example.sumoney.helpers;

import org.example.sumoney.dto.requests.DelegationFilterRequest;
import org.example.sumoney.entities.Delegation;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class DelegationSpecification {

    public static Specification<Delegation> filterBy(
            Long userId,
            DelegationFilterRequest filter
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(root.get("user").get("id"), userId)
            );

            if (filter.getContains() != null && !filter.getContains().isBlank()) {
                String contains = "%" + filter.getContains().trim().toLowerCase() + "%";

                Predicate titleContains = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        contains
                );

                Predicate destinationContains = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("destination")),
                        contains
                );

                Predicate descriptionContains = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        contains
                );

                predicates.add(
                        criteriaBuilder.or(
                                titleContains,
                                destinationContains,
                                descriptionContains
                        )
                );
            }

            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + filter.getTitle().trim().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getDestination() != null && !filter.getDestination().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("destination")),
                                "%" + filter.getDestination().trim().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getDateFrom() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("endDate"),
                                filter.getDateFrom()
                        )
                );
            }

            if (filter.getDateTo() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("startDate"),
                                filter.getDateTo()
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
