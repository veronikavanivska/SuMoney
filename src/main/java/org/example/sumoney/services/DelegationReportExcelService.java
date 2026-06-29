package org.example.sumoney.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.sumoney.dto.response.ExchangeRateResponse;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.Delegation;
import org.example.sumoney.entities.Expense;
import org.example.sumoney.repositories.DelegationRepository;
import org.example.sumoney.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DelegationReportExcelService {

    private static final String TEMPLATE_PATH = "/templates/expense-claim-template.xlsx";

    private final DelegationRepository delegationRepository;
    private final ExpenseRepository expenseRepository;
    private final ExchangeRateService exchangeRateService;

    public DelegationReportExcelService(
            DelegationRepository delegationRepository,
            ExpenseRepository expenseRepository,
            ExchangeRateService exchangeRateService
    ) {
        this.delegationRepository = delegationRepository;
        this.expenseRepository = expenseRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional(readOnly = true)
    public byte[] generateDelegationExpenseClaim(
            Long userId,
            Long delegationId,
            CurrencyCode targetCurrency
    ) {
        if (targetCurrency == null) {
            targetCurrency = CurrencyCode.PLN;
        }

        Delegation delegation = delegationRepository.findByIdAndUser_Id(
                        delegationId,
                        userId
                )
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));

        List<Expense> expenses = expenseRepository
                .findByDelegation_IdAndDelegation_User_IdOrderByExpenseDateAsc(
                        delegationId,
                        userId
                );

        try (
                InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH);
                Workbook workbook = templateStream != null
                        ? new XSSFWorkbook(templateStream)
                        : new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            Sheet sheet = getOrCreateSheet(workbook);

            fillHeader(sheet, delegation);
            fillExpenseRows(workbook, sheet, expenses, targetCurrency);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Could not generate delegation report", e);
        }
    }

    private Sheet getOrCreateSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() > 0) {
            return workbook.getSheetAt(0);
        }

        return workbook.createSheet("Expense Claim");
    }

    private void fillHeader(Sheet sheet, Delegation delegation) {
        setCellValue(sheet, 3, 1, getEmployeeName(delegation));
        setCellValue(sheet, 4, 1, LocalDate.now().toString());
        setCellValue(sheet, 5, 1, getClaimPeriod(delegation));
    }

    private String getEmployeeName(Delegation delegation) {
        if (delegation.getUser() != null && delegation.getUser().getEmail() != null) {
            return delegation.getUser().getEmail();
        }

        return "Employee";
    }

    private String getClaimPeriod(Delegation delegation) {
        LocalDate start = delegation.getStartDate();
        LocalDate end = delegation.getEndDate();

        if (start == null && end == null) {
            return "";
        }

        if (start != null && end != null) {
            return start + " - " + end;
        }

        if (start != null) {
            return start.toString();
        }

        return end.toString();
    }

    private void fillExpenseRows(
            Workbook workbook,
            Sheet sheet,
            List<Expense> expenses,
            CurrencyCode targetCurrency
    ) {
        int firstDataRowIndex = 9;

        setCellValue(sheet, 7, 3, "Expense Amount");
        setCellValue(sheet, 7, 4, "Exchange Rate");
        setCellValue(sheet, 7, 5, "Expense in " + targetCurrency);
        setCellValue(sheet, 7, 7, "Total amount in " + targetCurrency);
        int currentRowIndex = firstDataRowIndex;

        Row templateRow = sheet.getRow(firstDataRowIndex);

        clearReportArea(sheet, firstDataRowIndex, 7);

        CellStyle summaryStyle = createSummaryStyle(workbook, templateRow);
        Map<CurrencyCode, CellStyle> moneyStyles = new EnumMap<>(CurrencyCode.class);
        CellStyle rateStyle = createRateStyle(workbook);

        BigDecimal total = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            Row row = getOrCreateRow(sheet, currentRowIndex);

            copyRowStyle(templateRow, row);

            CurrencyCode originalCurrency = expense.getCurrency() != null
                    ? expense.getCurrency()
                    : CurrencyCode.PLN;

            LocalDate rateDate = expense.getExpenseDate() != null
                    ? expense.getExpenseDate()
                    : LocalDate.now();

            ExchangeRateResponse rate = exchangeRateService.getRate(
                    originalCurrency,
                    targetCurrency,
                    rateDate
            );

            BigDecimal convertedAmount = expense.getAmount()
                    .multiply(rate.getRate())
                    .setScale(2, RoundingMode.HALF_UP);

            total = total.add(convertedAmount);

            setCellValue(row, 0, formatDateRange(expense.getExpenseDate()));
            setCellValue(row, 1, delegationCategoryText(expense));
            setCellValue(row, 2, expense.getTitle());
            setCellValue(row, 3, expense.getAmount());
            setCellValue(row, 4, rate.getRate());
            setCellValue(row, 5, convertedAmount);
            setCellValue(row, 6, expense.getCategory() != null ? expense.getCategory().name() : "");
            setCellValue(row, 7, convertedAmount);

            row.getCell(3).setCellStyle(
                    getMoneyStyle(workbook, originalCurrency, moneyStyles)
            );

            row.getCell(4).setCellStyle(rateStyle);

            row.getCell(5).setCellStyle(
                    getMoneyStyle(workbook, targetCurrency, moneyStyles)
            );

            row.getCell(7).setCellStyle(
                    getMoneyStyle(workbook, targetCurrency, moneyStyles)
            );

            currentRowIndex++;
        }

        Row summaryRow = getOrCreateRow(sheet, currentRowIndex);
        copyRowStyle(templateRow, summaryRow);

        setCellValue(summaryRow, 0, "Summary");
        setCellValue(summaryRow, 7, total.setScale(2, RoundingMode.HALF_UP));

        for (int i = 0; i <= 7; i++) {
            Cell cell = getOrCreateCell(summaryRow, i);
            cell.setCellStyle(summaryStyle);
        }

        Cell summaryAmountCell = getOrCreateCell(summaryRow, 7);
        summaryAmountCell.setCellStyle(
                getMoneyStyle(workbook, targetCurrency, moneyStyles)
        );

        Row signatureRow = getOrCreateRow(sheet, currentRowIndex + 2);
        setCellValue(signatureRow, 0, "Employee Name & Signature");
        setCellValue(signatureRow, 2, "");

        for (int i = 0; i <= 7; i++) {
            sheet.autoSizeColumn(i);
        }

        sheet.setColumnWidth(2, 9000);
    }

    private CellStyle getMoneyStyle(
            Workbook workbook,
            CurrencyCode currencyCode,
            Map<CurrencyCode, CellStyle> moneyStyles
    ) {
        return moneyStyles.computeIfAbsent(
                currencyCode,
                currency -> createMoneyStyle(workbook, currency)
        );
    }

    private CellStyle createMoneyStyle(Workbook workbook, CurrencyCode currencyCode) {
        CellStyle style = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();

        style.setDataFormat(dataFormat.getFormat(getCurrencyFormat(currencyCode)));

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private String getCurrencyFormat(CurrencyCode currencyCode) {
        if (currencyCode == null) {
            return "#,##0.00";
        }

        return switch (currencyCode) {
            case PLN -> "#,##0.00 \"zł\"";
            case EUR -> "€ #,##0.00";
            case USD -> "$ #,##0.00";
            case GBP -> "£ #,##0.00";
            case CZK -> "#,##0.00 \"Kč\"";
            case UAH -> "₴ #,##0.00";
        };
    }

    private void clearReportArea(Sheet sheet, int fromRowIndex, int lastColumnIndex) {
        int lastRowIndex = sheet.getLastRowNum();

        for (int rowIndex = fromRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            for (int columnIndex = 0; columnIndex <= lastColumnIndex; columnIndex++) {
                Cell cell = row.getCell(columnIndex);

                if (cell != null) {
                    cell.setBlank();
                }
            }
        }
    }

    private String delegationCategoryText(Expense expense) {
        if (expense.getCategory() == null) {
            return "";
        }

        return expense.getCategory().name();
    }

    private String formatDateRange(LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DateTimeFormatter.ISO_DATE);
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();

        style.setDataFormat(dataFormat.getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createRateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();

        style.setDataFormat(dataFormat.getFormat("0.00000000"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createSummaryStyle(Workbook workbook, Row templateRow) {
        CellStyle style = workbook.createCellStyle();

        if (templateRow != null && templateRow.getCell(0) != null) {
            style.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
        }

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private void copyRowStyle(Row sourceRow, Row targetRow) {
        if (sourceRow == null) {
            return;
        }

        targetRow.setHeight(sourceRow.getHeight());

        for (int i = 0; i <= 7; i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = getOrCreateCell(targetRow, i);

            if (sourceCell != null) {
                targetCell.setCellStyle(sourceCell.getCellStyle());
            }
        }
    }

    private void setCellValue(Sheet sheet, int rowIndex, int columnIndex, String value) {
        Row row = getOrCreateRow(sheet, rowIndex);
        setCellValue(row, columnIndex, value);
    }

    private void setCellValue(Row row, int columnIndex, String value) {
        Cell cell = getOrCreateCell(row, columnIndex);
        cell.setCellValue(value != null ? value : "");
    }

    private void setCellValue(Row row, int columnIndex, BigDecimal value) {
        Cell cell = getOrCreateCell(row, columnIndex);

        if (value == null) {
            cell.setBlank();
            return;
        }

        cell.setCellValue(value.doubleValue());
    }

    private Row getOrCreateRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);

        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        return row;
    }

    private Cell getOrCreateCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            cell = row.createCell(columnIndex);
        }

        return cell;
    }
}