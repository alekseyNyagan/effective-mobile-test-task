package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityTestConfig;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityTestConfig.class)
@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void transfer_success() throws Exception {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.50");

        TransferRequest request = new TransferRequest(fromCardId, toCardId, amount);

        doNothing().when(transactionService).transferBetweenCards(fromCardId, toCardId, amount);

        mockMvc.perform(post("/transfer/transfer")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(transactionService).transferBetweenCards(fromCardId, toCardId, amount);
    }

    @Test
    void transfer_unauthorized() throws Exception {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.50");

        TransferRequest request = new TransferRequest(fromCardId, toCardId, amount);

        mockMvc.perform(post("/transfer/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(transactionService, never()).transferBetweenCards(any(), any(), any());
    }

    @Test
    void transfer_forbidden() throws Exception {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.50");

        TransferRequest request = new TransferRequest(fromCardId, toCardId, amount);

        mockMvc.perform(post("/transfer/transfer")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(transactionService, never()).transferBetweenCards(any(), any(), any());
    }

    @Test
    void transfer_invalidFromCardId() throws Exception {
        Long invalidFromCardId = -1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.50");

        TransferRequest request = new TransferRequest(invalidFromCardId, toCardId, amount);

        mockMvc.perform(post("/transfer/transfer")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).transferBetweenCards(any(), any(), any());
    }

    @Test
    void transfer_invalidAmount() throws Exception {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal invalidAmount = BigDecimal.ZERO;

        TransferRequest request = new TransferRequest(fromCardId, toCardId, invalidAmount);

        mockMvc.perform(post("/transfer/transfer")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).transferBetweenCards(any(), any(), any());
    }
}
