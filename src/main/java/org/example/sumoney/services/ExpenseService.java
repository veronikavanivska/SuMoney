package org.example.sumoney.services;

import org.example.sumoney.dto.requests.CreateExpenseRequest;
import org.example.sumoney.dto.requests.ExpenseFilterRequest;
import org.example.sumoney.dto.response.ExpenseResponse;
import org.example.sumoney.entities.Delegation;
import org.example.sumoney.entities.Expense;
import org.example.sumoney.helpers.ExpenseSpecification;
import org.example.sumoney.repositories.DelegationRepository;
import org.example.sumoney.repositories.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final DelegationRepository delegationRepository;
    private final FileStorageService fileStorageService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            DelegationRepository delegationRepository,
            FileStorageService fileStorageService
    ) {
        this.expenseRepository = expenseRepository;
        this.delegationRepository = delegationRepository;
        this.fileStorageService = fileStorageService;
    }

    public ExpenseResponse createExpense(
            Long userId,
            Long delegationId,
            CreateExpenseRequest request
    ) {
        validateCreateExpenseRequest(request);

        Delegation delegation = delegationRepository.findByIdAndUser_Id(delegationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNote(request.getNote());
        expense.setDelegation(delegation);

        MultipartFile receiptImage = request.getReceiptImage();

        if (receiptImage != null && !receiptImage.isEmpty()) {
            FileStorageService.StoredFile storedFile =
                    fileStorageService.uploadReceipt(receiptImage);

            expense.setReceiptObjectName(storedFile.objectName());
            expense.setReceiptContentType(storedFile.contentType());
        }

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    public Page<ExpenseResponse> findExpenses(
            Long userId,
            Long delegationId,
            ExpenseFilterRequest filter,
            Pageable pageable
    ) {
        if (filter == null) {
            filter = new ExpenseFilterRequest();
        }

        validateExpenseFilter(filter);

        return expenseRepository
                .findAll(
                        ExpenseSpecification.filterBy(userId, delegationId, filter),
                        pageable
                )
                .map(this::mapToResponse);
    }

    public ExpenseResponse getExpenseById(
            Long userId,
            Long delegationId,
            Long expenseId
    ) {
        Expense expense = expenseRepository
                .findByIdAndDelegation_IdAndDelegation_User_Id(
                        expenseId,
                        delegationId,
                        userId
                )
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        return mapToResponse(expense);
    }

    public String deleteExpense(Long userId, Long delegationId, Long expenseId
    ) {
        Expense expense = expenseRepository
                .findByIdAndDelegation_IdAndDelegation_User_Id(
                        expenseId,
                        delegationId,
                        userId
                )
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (expense.getReceiptObjectName() != null) {
            fileStorageService.deleteFile(expense.getReceiptObjectName());
        }

        expenseRepository.delete(expense);

        return "Deleted expense";
    }

    public FileStorageService.StoredBytes getReceiptFile(Long userId, Long delegationId, Long expenseId) {
        Expense expense = expenseRepository
                .findByIdAndDelegation_IdAndDelegation_User_Id(
                        expenseId,
                        delegationId,
                        userId
                )
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (expense.getReceiptObjectName() == null) {
            throw new IllegalArgumentException("Receipt not found");
        }

        return fileStorageService.getFileBytes(
                expense.getReceiptObjectName(),
                expense.getReceiptContentType()
        );
    }

    private void validateCreateExpenseRequest(CreateExpenseRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        if (request.getCurrency() == null) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    private void validateExpenseFilter(ExpenseFilterRequest filter) {
        if (filter.getAmountFrom() != null && filter.getAmountTo() != null) {
            if (filter.getAmountTo().compareTo(filter.getAmountFrom()) < 0) {
                throw new IllegalArgumentException("amountTo cannot be lower than amountFrom");
            }
        }

        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            if (filter.getDateTo().isBefore(filter.getDateFrom())) {
                throw new IllegalArgumentException("dateTo cannot be before dateFrom");
            }
        }
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .note(expense.getNote())
                .hasReceipt(expense.getReceiptObjectName() != null)
                .receiptContentType(expense.getReceiptContentType())
                .delegationId(expense.getDelegation().getId())
                .build();
    }
}