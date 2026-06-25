package org.example.sumoney.helpers;

import org.example.sumoney.dto.requests.ExpenseFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.example.sumoney.entities.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ExpenseSpecification {

    public static Specification<Expense> filterBy(
            Long userId,
            Long delegationId,
            ExpenseFilterRequest filter
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(root.get("delegation").get("id"), delegationId)
            );

            predicates.add(
                    criteriaBuilder.equal(root.get("delegation").get("user").get("id"), userId)
            );

            if (filter.getContains() != null && !filter.getContains().isBlank()) {
                String contains = "%" + filter.getContains().trim().toLowerCase() + "%";

                Predicate titleContains = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        contains
                );

                Predicate noteContains = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("note")),
                        contains
                );

                predicates.add(
                        criteriaBuilder.or(titleContains, noteContains)
                );
            }

            if (filter.getCategory() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("category"), filter.getCategory())
                );
            }

            if (filter.getCurrency() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("currency"), filter.getCurrency())
                );
            }

            if (filter.getAmountFrom() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("amount"),
                                filter.getAmountFrom()
                        )
                );
            }

            if (filter.getAmountTo() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("amount"),
                                filter.getAmountTo()
                        )
                );
            }

            if (filter.getDateFrom() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("expenseDate"),
                                filter.getDateFrom()
                        )
                );
            }

            if (filter.getDateTo() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("expenseDate"),
                                filter.getDateTo()
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}