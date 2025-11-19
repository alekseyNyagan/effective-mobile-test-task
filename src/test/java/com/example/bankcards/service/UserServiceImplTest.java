package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserFilter;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");
        user.setPhoneNumber("+79999999999");

        userDto = new UserDto(
                "+79999999999",
                "password123",
                "John",
                "Doe",
                Set.of("USER")
        );

        userResponseDto = new UserResponseDto(
                "+79999999999",
                "John",
                "Doe"
        );
    }

    @Test
    void getAll_success() {
        UserFilter filter = new UserFilter(null);
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        Page<UserResponseDto> result = userService.getAll(filter, Pageable.unpaged());

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getOne_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getOne(1L);

        assertNotNull(result);
        assertEquals(userResponseDto.name(), result.name());
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponseDto(user);
    }

    @Test
    void getOne_notFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getOne(1L));
        verify(userRepository).findById(1L);
    }

    @Test
    void create_success() {
        when(userMapper.toEntity(any(UserDto.class), any(), any())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(userResponseDto);

        UserResponseDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals(userResponseDto.name(), result.name());
        verify(userRepository).save(user);
    }

    @Test
    void patch_updatePassword_success() throws IOException {
        ObjectNode patchNode = mock(ObjectNode.class);
        when(patchNode.has("password")).thenReturn(true);
        when(patchNode.get("password")).thenReturn(mock(JsonNode.class));
        when(patchNode.get("password").asText()).thenReturn("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        userService.patch(1L, patchNode);

        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void patch_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        JsonNode patchNode = mock(JsonNode.class);

        assertThrows(ResponseStatusException.class, () -> userService.patch(1L, patchNode));
    }

    @Test
    void patchMany_success() throws IOException {
        List<Long> ids = List.of(1L);
        ObjectNode patchNode = mock(ObjectNode.class);
        when(patchNode.has("password")).thenReturn(true);
        when(patchNode.get("password")).thenReturn(mock(JsonNode.class));
        when(patchNode.get("password").asText()).thenReturn("newPassword");

        when(userRepository.findAllById(ids)).thenReturn(List.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(userRepository.saveAll(anyCollection())).thenReturn(List.of(user));

        List<Long> result = userService.patchMany(ids, patchNode);

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0));
        verify(userRepository).saveAll(anyCollection());
    }

    @Test
    void delete_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);
        doNothing().when(userRepository).delete(user);

        UserResponseDto result = userService.delete(1L);

        assertNotNull(result);
        verify(userRepository).delete(user);
    }

    @Test
    void delete_notFound_returnsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserResponseDto result = userService.delete(1L);

        assertNull(result);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteMany_success() {
        List<Long> ids = List.of(1L, 2L);
        doNothing().when(userRepository).deleteAllById(ids);

        userService.deleteMany(ids);

        verify(userRepository).deleteAllById(ids);
    }
}
