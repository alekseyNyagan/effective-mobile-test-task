package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityTestConfig;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityTestConfig.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_success() throws Exception {
        Page<UserResponseDto> usersPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userService.getAll(any(), any())).thenReturn(usersPage);

        mockMvc.perform(get("/user/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());

        verify(userService).getAll(any(), any());
    }

    @Test
    void getAll_forbidden() throws Exception {
        mockMvc.perform(get("/user/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAll(any(), any());
    }

    @Test
    void getOne_success() throws Exception {
        Long userId = 1L;
        UserResponseDto userResponseDto = new UserResponseDto("+79999999999", "John", "Doe");


        when(userService.getOne(userId)).thenReturn(userResponseDto);

        mockMvc.perform(get("/user/v1/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+79999999999"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"));

        verify(userService).getOne(userId);
    }

    @Test
    void create_success() throws Exception {
        UserDto userDto = new UserDto(
                "+79999999999",
                "password123",
                "John",
                "Doe",
                Set.of("USER")
        );

        UserResponseDto createdUserDto = new UserResponseDto("+79999999999", "John", "Doe");

        when(userService.create(any(UserDto.class))).thenReturn(createdUserDto);

        mockMvc.perform(post("/user/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+79999999999"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"));

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void create_invalidPhoneNumber() throws Exception {
        UserDto userDto = new UserDto(
                "123",
                "password123",
                "John",
                "Doe",
                Set.of("USER")
        );

        mockMvc.perform(post("/user/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void patch_success() throws Exception {
        Long userId = 1L;
        String patchJson = """
                {
                    "name": "Jane"
                }
                """;

        UserResponseDto userResponseDto = new UserResponseDto(null, "Jane", null);

        when(userService.patch(eq(userId), any(JsonNode.class))).thenReturn(userResponseDto);

        mockMvc.perform(patch("/user/v1/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk());

        verify(userService).patch(eq(userId), any(JsonNode.class));
    }

    @Test
    void patchMany_success() throws Exception {
        List<Long> userIds = List.of(1L, 2L, 3L);
        String patchJson = """
                {
                    "name": "Jane"
                }
                """;

        when(userService.patchMany(eq(userIds), any(JsonNode.class))).thenReturn(userIds);

        mockMvc.perform(patch("/user/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("ids", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk());

        verify(userService).patchMany(eq(userIds), any(JsonNode.class));
    }

    @Test
    void delete_success() throws Exception {
        Long userId = 1L;
        UserResponseDto userResponseDto = new UserResponseDto(null, null, null);

        when(userService.delete(userId)).thenReturn(userResponseDto);

        mockMvc.perform(delete("/user/v1/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(userService).delete(userId);
    }

    @Test
    void deleteMany_success() throws Exception {
        List<Long> userIds = List.of(1L, 2L, 3L);

        doNothing().when(userService).deleteMany(userIds);

        mockMvc.perform(delete("/user/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk());

        verify(userService).deleteMany(userIds);
    }
}
