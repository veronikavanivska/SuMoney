package org.example.sumoney.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table(name = "expense")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    private LocalDate expenseDate;

    private String note;

    @Column(name = "receipt_object_name")
    private String receiptObjectName;

    @Column(name = "receipt_content_type")
    private String receiptContentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegation_id")
    private Delegation delegation;

}
