package com.receiptmanager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReceiptDtos {

    public record ReceiptItemRequest(
            @NotBlank String itemName,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
            @NotNull @Min(1) Integer quantity) {
    }

    public record ReceiptRequest(
            @NotBlank String storeName,
            @NotNull LocalDate receiptDate,
            @NotBlank String category,
            @NotEmpty @Valid List<ReceiptItemRequest> items) {
    }

    public record ReceiptItemResponse(Long id, String itemName, BigDecimal price, Integer quantity, BigDecimal lineTotal) {
    }

    public record ReceiptResponse(
            Long id,
            String storeName,
            LocalDate receiptDate,
            String category,
            List<ReceiptItemResponse> items,
            BigDecimal total) {
    }
}
