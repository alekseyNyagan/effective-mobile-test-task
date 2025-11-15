package com.example.bankcards.dto;

import java.util.Set;

/**
 * DTO for {@link com.example.bankcards.entity.User}
 */
public record UserDto(String phoneNumber, String password, String name, String surname, Set<String> roleNames) {
}