package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserFilter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface UserService {
    Page<UserResponseDto> getAll(UserFilter filter, Pageable pageable);

    UserResponseDto getOne(Long id);

    UserResponseDto create(UserDto userDto);

    UserResponseDto patch(Long id, JsonNode patchNode) throws IOException;

    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

    UserResponseDto delete(Long id);

    void deleteMany(List<Long> ids);
}
