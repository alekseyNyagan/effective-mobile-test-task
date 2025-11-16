package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionRepository;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ForbiddenOperationException;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final CardRepository cardRepository;

    private final TransactionRepository transactionRepository;

    @Override
    public void transferBetweenCards(Long fromId, Long toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ForbiddenOperationException("Invalid amount");
        }

        Card from = cardRepository.findById(fromId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        Card to = cardRepository.findById(toId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!from.getOwner().getId().equals(to.getOwner().getId())) {
            throw new ForbiddenOperationException("Cards belong to different users");
        }

        if (from.getCardStatus() == CardStatus.BLOCKED) {
            throw new ForbiddenOperationException("Source card is blocked");
        }

        if (from.getMoneyAmount().compareTo(amount) < 0) {
            throw new ForbiddenOperationException("Not enough funds");
        }

        from.setMoneyAmount(from.getMoneyAmount().subtract(amount));

        to.setMoneyAmount(to.getMoneyAmount().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);

        Transaction transaction = new Transaction(from, to, amount, LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
