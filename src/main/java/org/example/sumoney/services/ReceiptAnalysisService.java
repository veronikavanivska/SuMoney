package org.example.sumoney.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sumoney.dto.response.ReceiptAnalysisResponse;
import org.example.sumoney.entities.CurrencyCode;
import org.example.sumoney.entities.ExpenseCategory;
import org.example.sumoney.repositories.DelegationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReceiptAnalysisService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final DelegationRepository delegationRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public ReceiptAnalysisService(
            DelegationRepository delegationRepository,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.delegationRepository = delegationRepository;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public ReceiptAnalysisResponse analyzeReceipt(
            Long userId,
            Long delegationId,
            MultipartFile receiptImage
    ) {
        validateUserHasDelegation(userId, delegationId);
        validateFile(receiptImage);

        try {
            String base64File = Base64.getEncoder()
                    .encodeToString(receiptImage.getBytes());

            Map<String, Object> requestBody = buildGeminiRequestBody(
                    receiptImage.getContentType(),
                    base64File
            );

            GeminiResponse geminiResponse = restClient.post()
                    .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiResponse.class);

            String jsonText = extractText(geminiResponse);

            return parseReceiptAnalysis(jsonText);

        } catch (Exception e) {
            throw new RuntimeException("Could not analyze receipt", e);
        }
    }

    private void validateUserHasDelegation(Long userId, Long delegationId) {
        delegationRepository.findByIdAndUser_Id(delegationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Receipt file is required");
        }

        if (file.getContentType() == null ||
                !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported receipt file type");
        }

        long maxSize = 8 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Receipt file is too large");
        }
    }

    private Map<String, Object> buildGeminiRequestBody(
            String mimeType,
            String base64File
    ) {
        return Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", buildPrompt()),
                                        Map.of(
                                                "inline_data",
                                                Map.of(
                                                        "mime_type", mimeType,
                                                        "data", base64File
                                                )
                                        )
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "response_mime_type", "application/json"
                )
        );
    }

    private String buildPrompt() {
        return """
                You are an expense receipt extraction assistant for a business travel expense app.

                Analyze the attached receipt, invoice, ticket, hotel bill or payment confirmation.

                Return ONLY valid JSON. Do not include markdown. Do not include explanations.

                Required JSON schema:
                {
                  "title": "short expense title",
                  "amount": 0.00,
                  "currency": "EUR",
                  "expenseDate": "YYYY-MM-DD",
                  "category": "FOOD",
                  "note": "short business expense note",
                  "confidence": "HIGH",
                  "warnings": []
                }

                Rules:
                - amount must be the final total amount paid by the customer.
                - currency must be one of: PLN, EUR, USD, GBP, CHF, CZK, DKK, NOK, SEK, HUF, UAH.
                - category must be one of: FOOD, HOTEL, TRANSPORT, FUEL, PARKING, TICKETS, TAXI, OFFICE_SUPPLIES, OTHER.
                - expenseDate must be the date of the expense, not the print date unless it is the only date.
                - If you cannot identify a value, use null.
                - confidence must be HIGH, MEDIUM or LOW.
                - warnings should contain short warnings when something is uncertain.
                - Prefer concise title, for example: "Hotel Munich", "Taxi airport", "Train ticket", "Restaurant".
                """;
    }

    private String extractText(GeminiResponse response) {
        if (response == null ||
                response.candidates() == null ||
                response.candidates().isEmpty()) {
            throw new IllegalArgumentException("Empty Gemini response");
        }

        GeminiCandidate candidate = response.candidates().get(0);

        if (candidate.content() == null ||
                candidate.content().parts() == null ||
                candidate.content().parts().isEmpty()) {
            throw new IllegalArgumentException("Invalid Gemini response");
        }

        String text = candidate.content().parts().get(0).text();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Empty Gemini response text");
        }

        return text;
    }

    private ReceiptAnalysisResponse parseReceiptAnalysis(String jsonText) throws Exception {
        JsonNode root = objectMapper.readTree(jsonText);

        String title = getTextOrNull(root, "title");
        BigDecimal amount = getBigDecimalOrNull(root, "amount");
        CurrencyCode currency = getCurrencyOrDefault(root, "currency");
        LocalDate expenseDate = getDateOrNull(root, "expenseDate");
        ExpenseCategory category = getCategoryOrDefault(root, "category");
        String note = getTextOrNull(root, "note");
        String confidence = getTextOrDefault(root, "confidence", "LOW");

        List<String> warnings = objectMapper.convertValue(
                root.path("warnings"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        return ReceiptAnalysisResponse.builder()
                .title(title)
                .amount(amount)
                .currency(currency)
                .expenseDate(expenseDate)
                .category(category)
                .note(note)
                .confidence(confidence)
                .warnings(warnings)
                .build();
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        String value = getTextOrNull(node, field);
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    private BigDecimal getBigDecimalOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.decimalValue();
    }

    private LocalDate getDateOrNull(JsonNode node, String field) {
        String value = getTextOrNull(node, field);

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private CurrencyCode getCurrencyOrDefault(JsonNode node, String field) {
        String value = getTextOrNull(node, field);

        if (value == null || value.isBlank()) {
            return CurrencyCode.PLN;
        }

        try {
            return CurrencyCode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CurrencyCode.PLN;
        }
    }

    private ExpenseCategory getCategoryOrDefault(JsonNode node, String field) {
        String value = getTextOrNull(node, field);

        if (value == null || value.isBlank()) {
            return ExpenseCategory.OTHER;
        }

        try {
            return ExpenseCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ExpenseCategory.OTHER;
        }
    }

    public record GeminiResponse(
            List<GeminiCandidate> candidates
    ) {
    }

    public record GeminiCandidate(
            GeminiContent content
    ) {
    }

    public record GeminiContent(
            List<GeminiPart> parts
    ) {
    }

    public record GeminiPart(
            String text
    ) {
    }
}