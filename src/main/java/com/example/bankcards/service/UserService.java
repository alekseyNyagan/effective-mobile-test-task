package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserFilter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface UserService {
    Page<User> getAll(UserFilter filter, Pageable pageable);

    UserDto getOne(Long id);

    List<User> getMany(List<Long> ids);

    UserDto create(UserDto userDto);

    User patch(Long id, JsonNode patchNode) throws IOException;

    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

    User delete(Long id);

    void deleteMany(List<Long> ids);
}
