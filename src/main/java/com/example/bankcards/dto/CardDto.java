package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * DTO for {@link com.example.bankcards.entity.Card}
 */
public record CardDto(
        @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$", message = "Card number must be in format 'XXXX XXXX XXXX XXXX'")
        @NotBlank(message = "Card number cannot be empty") String cardNumber,
        @NotNull(message = "Expiry date is required") @Future(message = "Expiry date must be in the future") YearMonth expiry,
        @NotNull(message = "Money amount is required") @Digits(integer = 12, fraction = 2, message = "Invalid money format")
        @PositiveOrZero(message = "Money amount cannot be negative") BigDecimal moneyAmount,
        @NotNull(message = "Card status is required") CardStatus cardStatus,
        @NotBlank(message = "Owner phone number cannot be empty")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must contain from 10 to 15 digits and optional +") String ownerPhoneNumber) {
}