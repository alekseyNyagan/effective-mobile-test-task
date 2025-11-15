package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    private final UserMapper userMapper;

    @Override
    public Page<User> getAll(UserFilter filter, Pageable pageable) {
        Specification<User> spec = filter.toSpecification();
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public UserDto getOne(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
         return userMapper.toUserDto(userOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    @Override
    public List<User> getMany(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    public User patch(Long id, JsonNode patchNode) throws IOException {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        objectMapper.readerForUpdating(user).readValue(patchNode);

        return userRepository.save(user);
    }

    @Override
    public List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException {
        Collection<User> users = userRepository.findAllById(ids);

        for (User user : users) {
            objectMapper.readerForUpdating(user).readValue(patchNode);
        }

        List<User> resultUsers = userRepository.saveAll(users);
        return resultUsers.stream()
                .map(User::getId)
                .toList();
    }

    @Override
    public User delete(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
        return user;
    }

    @Override
    public void deleteMany(List<Long> ids) {
        userRepository.deleteAllById(ids);
    }
}
