package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * DTO for {@link com.example.bankcards.entity.Card}
 */
public record CardDto(String cardNumber, YearMonth expiry,
                      BigDecimal moneyAmount,
                      CardStatus cardStatus, String ownerPhoneNumber) {
}