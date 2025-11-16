package com.example.bankcards.service;

import java.math.BigDecimal;

public interface TransactionService {
    void transferBetweenCards(Long fromId, Long toId, BigDecimal amount);
}
