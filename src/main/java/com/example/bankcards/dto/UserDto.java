package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO for {@link com.example.bankcards.entity.User}
 */
public record UserDto(@Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Phone number must contain only digits (0–9), optionally starting with +, and be 10–15 characters long") String phoneNumber,
                      @NotBlank(message = "Password cannot be empty")
                      @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters long") String password,
                      @NotBlank(message = "Name cannot be empty") @Size(max = 50, message = "Name must be at most 50 characters long") String name,
                      @NotBlank(message = "Surname cannot be empty") @Size(max = 50, message = "Surname must be at most 50 characters long") String surname,
                      @NotNull(message = "Roles cannot be null") @Size(min = 1, message = "User must have at least one role") Set<String> roleNames) {
}