package com.example.bankcards.service;

import com.example.bankcards.controller.CardFilter;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.AdminCardFilter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface CardService {
    Page<Card> getAll(AdminCardFilter filter, Pageable pageable);

    Page<Card> getAllMyCards(CardFilter filter, Pageable pageable);

    CardDto getOne(Long id);

    CardDto create(CardDto dto);

    CardDto patch(Long id, JsonNode patchNode) throws IOException;

    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

    CardDto delete(Long id);

    void deleteMany(List<Long> ids);

    void createBlockRequest(Long id);

    void approveBlockRequest(Long id);

    BigDecimal getBalance(Long cardId);
}
