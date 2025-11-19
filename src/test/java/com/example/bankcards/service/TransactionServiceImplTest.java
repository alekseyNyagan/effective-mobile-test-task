package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setCardStatus(CardStatus.ACTIVE);
        fromCard.setMoneyAmount(new BigDecimal("1000.00"));
        fromCard.setOwner(user);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setCardStatus(CardStatus.ACTIVE);
        toCard.setMoneyAmount(new BigDecimal("500.00"));
        toCard.setOwner(user);
    }

    @Test
    void transferBetweenCards_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        BigDecimal amount = new BigDecimal("100.00");

        transactionService.transferBetweenCards(1L, 2L, amount);

        assertEquals(new BigDecimal("900.00"), fromCard.getMoneyAmount());
        assertEquals(new BigDecimal("600.00"), toCard.getMoneyAmount());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transactionRepository).save(any());
    }

    @Test
    void transferBetweenCards_differentUsers_throwsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        toCard.setOwner(anotherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(ForbiddenOperationException.class, () -> transactionService.transferBetweenCards(1L, 2L, amount));
        verify(cardRepository, never()).save(any(Card.class));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenCards_insufficientFunds_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        BigDecimal amount = new BigDecimal("2000.00");

        assertThrows(ForbiddenOperationException.class, () -> transactionService.transferBetweenCards(1L, 2L, amount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_cardBlocked_throwsException() {
        fromCard.setCardStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(ForbiddenOperationException.class, () -> transactionService.transferBetweenCards(1L, 2L, amount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_cardNotFound_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(EntityNotFoundException.class, () -> transactionService.transferBetweenCards(1L, 2L, amount));
        verify(cardRepository, never()).save(any(Card.class));
    }
}
