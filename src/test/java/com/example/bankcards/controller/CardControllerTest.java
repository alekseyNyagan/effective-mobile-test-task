package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityTestConfig;
import com.example.bankcards.dto.BlockCardRequestDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityTestConfig.class)
@WebMvcTest(controllers = CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_success() throws Exception {
        String dto = """
                {
                    "cardNumber": "1234 5678 9012 3456",
                    "expiry": "2025-12",
                    "moneyAmount": 10000.0,
                    "cardStatus": "ACTIVE",
                    "ownerPhoneNumber": "+79999999999"
                }""";

        CardDto cardDto = new CardDto(
                "**** **** **** 3456",
                YearMonth.of(2025, 12),
                new BigDecimal("10000.0"),
                CardStatus.ACTIVE,
                "+79999999999"
        );

        when(cardService.create(any())).thenReturn(cardDto);

        mockMvc.perform(post("/card/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.expiry").value("2025-12"))
                .andExpect(jsonPath("$.moneyAmount").value(10000.0))
                .andExpect(jsonPath("$.ownerPhoneNumber").value("+79999999999"))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"));
    }

    @Test
    void create_forbidden() throws Exception {
        String dto = """
                {
                    "cardNumber": "1234 5678 9012 3456",
                    "expiry": "2025-12",
                    "moneyAmount": 10000.0,
                    "cardStatus": "ACTIVE",
                    "ownerPhoneNumber": "+79999999999"
                }""";

        mockMvc.perform(post("/card/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(cardService, never()).create(any());
    }

    @Test
    void getAll_adminAllowed() throws Exception {
        when(cardService.getAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/card/v1/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(cardService).getAll(any(), any());
    }

    @Test
    void getAll_userForbidden() throws Exception {
        mockMvc.perform(get("/card/v1/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());

        verify(cardService, never()).getAll(any(), any());
    }

    @Test
    void getAllMyCards_success() throws Exception {
        when(cardService.getAllMyCards(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/card/v1/myCards")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        verify(cardService).getAllMyCards(any(), any());
    }

    @Test
    void getOne_success() throws Exception {
        Long cardId = 1L;
        CardDto cardDto = new CardDto(
                "**** **** **** 3456",
                YearMonth.of(2025, 12),
                new BigDecimal("10000.0"),
                CardStatus.ACTIVE,
                "+79999999999"
        );

        when(cardService.getOne(cardId)).thenReturn(cardDto);

        mockMvc.perform(get("/card/v1/{id}", cardId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 3456"));

        verify(cardService).getOne(cardId);
    }

    @Test
    void patch_success() throws Exception {
        Long cardId = 1L;
        String patchJson = """
                {
                    "cardStatus": "BLOCKED"
                }
                """;

        CardDto cardDto = new CardDto(
                "**** **** **** 3456",
                YearMonth.of(2025, 12),
                new BigDecimal("10000.0"),
                CardStatus.BLOCKED,
                "+79999999999"
        );

        when(cardService.patch(eq(cardId), any(JsonNode.class))).thenReturn(cardDto);

        mockMvc.perform(patch("/card/v1/changeCardStatus/{id}", cardId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardStatus").value("BLOCKED"));

        verify(cardService).patch(eq(cardId), any(JsonNode.class));
    }

    @Test
    void patchMany_success() throws Exception {
        List<Long> cardIds = List.of(1L, 2L, 3L);
        String patchJson = """
                {
                    "cardStatus": "BLOCKED"
                }
                """;

        when(cardService.patchMany(eq(cardIds), any(JsonNode.class))).thenReturn(cardIds);

        mockMvc.perform(patch("/card/v1/changeManyCardStatus")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("ids", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk());

        verify(cardService).patchMany(eq(cardIds), any(JsonNode.class));
    }

    @Test
    void delete_success() throws Exception {
        Long cardId = 1L;
        CardDto cardDto = new CardDto(
                "**** **** **** 3456",
                YearMonth.of(2025, 12),
                new BigDecimal("10000.0"),
                CardStatus.ACTIVE,
                "+79999999999"
        );

        when(cardService.delete(cardId)).thenReturn(cardDto);

        mockMvc.perform(delete("/card/v1/{id}", cardId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(cardService).delete(cardId);
    }

    @Test
    void deleteMany_success() throws Exception {
        List<Long> cardIds = List.of(1L, 2L, 3L);

        doNothing().when(cardService).deleteMany(cardIds);

        mockMvc.perform(delete("/card/v1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk());

        verify(cardService).deleteMany(cardIds);
    }

    @Test
    void createBlockRequest_success() throws Exception {
        Long cardId = 1L;
        BlockCardRequestDto dto = new BlockCardRequestDto(cardId);

        doNothing().when(cardService).createBlockRequest(cardId);

        mockMvc.perform(post("/card/v1/block-request")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(cardService).createBlockRequest(cardId);
    }

    @Test
    void approveBlock_success() throws Exception {
        Long requestId = 1L;

        doNothing().when(cardService).approveBlockRequest(requestId);

        mockMvc.perform(post("/card/v1/block-request/{id}/approve", requestId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(cardService).approveBlockRequest(requestId);
    }

    @Test
    void getBalance_success() throws Exception {
        Long cardId = 1L;
        BigDecimal balance = new BigDecimal("5000.50");

        when(cardService.getBalance(cardId)).thenReturn(balance);

        mockMvc.perform(get("/card/v1/{cardId}/balance", cardId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5000.50));

        verify(cardService).getBalance(cardId);
    }
}
