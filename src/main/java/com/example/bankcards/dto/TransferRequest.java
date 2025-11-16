package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source card ID cannot be null") @Positive(message = "Source card ID must be a positive number") Long fromCardId,
        @NotNull(message = "Target card ID cannot be null") @Positive(message = "Target card ID must be a positive number") Long toCardId,
        @NotNull(message = "Amount cannot be null") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount) {
}
