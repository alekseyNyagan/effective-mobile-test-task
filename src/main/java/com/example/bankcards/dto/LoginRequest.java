package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(@Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Phone number must contain only digits (optionally starting with +) and be 10–15 characters long") String phoneNumber,
                           @NotBlank(message = "Password cannot be empty")
                           @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters long") String password) {
}
