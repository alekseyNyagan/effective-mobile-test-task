package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenOperationException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardBlockRequestRepository requestRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private CardDto cardDto;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        card = new Card();
        card.setId(1L);
        card.setOwner(user);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setMoneyAmount(new BigDecimal("1000.00"));

        cardDto = new CardDto(
                "1234567890123456",
                YearMonth.now(),
                new BigDecimal("1000.00"),
                CardStatus.ACTIVE,
                "+79999999999"
        );
    }

    @Test
    void getOne_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toCardDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getOne(1L);

        assertNotNull(result);
        assertEquals(cardDto.cardNumber(), result.cardNumber());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getOne_notFound_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cardService.getOne(1L));
        verify(cardRepository).findById(1L);
    }

    @Test
    void create_success() {
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(user));
        when(cardMapper.toEntity(any(CardDto.class), any(User.class))).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardDto(any(Card.class))).thenReturn(cardDto);

        CardDto result = cardService.create(cardDto);

        assertNotNull(result);
        assertEquals(cardDto.cardNumber(), result.cardNumber());
        verify(cardRepository).save(card);
    }

    @Test
    void getBalance_success() {
        when(cardRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(card));

        BigDecimal balance = cardService.getBalance(1L, user);

        assertEquals(card.getMoneyAmount(), balance);
        verify(cardRepository).findByIdAndOwner(1L, user);
    }

    @Test
    void getBalance_cardBlocked_throwsException() {
        card.setCardStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenOperationException.class, () -> cardService.getBalance(1L, user));
        verify(cardRepository).findByIdAndOwner(1L, user);
    }

    @Test
    void createBlockRequest_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(requestRepository.existsByCardAndStatus(card, BlockRequestStatus.PENDING)).thenReturn(false);

        cardService.createBlockRequest(1L, user);

        verify(requestRepository).save(any(CardBlockRequest.class));
    }

    @Test
    void createBlockRequest_alreadyExists_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(requestRepository.existsByCardAndStatus(card, BlockRequestStatus.PENDING)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> cardService.createBlockRequest(1L, user));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createBlockRequest_notOwner_throwsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenOperationException.class, () -> cardService.createBlockRequest(1L, anotherUser));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveBlockRequest_success() {
        CardBlockRequest request = new CardBlockRequest();
        request.setId(1L);
        request.setCard(card);
        request.setStatus(BlockRequestStatus.PENDING);

        ObjectNode node = mock(ObjectNode.class);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(objectMapper.createObjectNode()).thenReturn(node);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        cardService.approveBlockRequest(1L);

        assertEquals(BlockRequestStatus.APPROVED, request.getStatus());
        verify(requestRepository).save(request);
        verify(cardRepository).save(card);
    }

    @Test
    void approveBlockRequest_notFound_throwsException() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.approveBlockRequest(1L));
    }

    @Test
    void approveBlockRequest_alreadyProcessed_throwsException() {
        CardBlockRequest request = new CardBlockRequest();
        request.setStatus(BlockRequestStatus.APPROVED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class, () -> cardService.approveBlockRequest(1L));
    }

    @Test
    void patch_success() {
        ObjectNode patchNode = mock(ObjectNode.class);
        when(patchNode.has("cardStatus")).thenReturn(true);
        when(patchNode.get("cardStatus")).thenReturn(mock(JsonNode.class));
        when(patchNode.get("cardStatus").asText()).thenReturn("BLOCKED");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toCardDto(card)).thenReturn(cardDto);

        cardService.patch(1L, patchNode);

        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void delete_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(card);

        cardService.delete(1L);

        verify(cardRepository).delete(card);
    }
}
