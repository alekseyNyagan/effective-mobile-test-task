package com.example.bankcards.service;

import com.example.bankcards.controller.CardFilter;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.AdminCardFilter;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class CardServiceImpl implements CardService {

    public static final String CARD_STATUS = "cardStatus";

    private final CardMapper cardMapper;

    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    @Override
    public Page<Card> getAll(AdminCardFilter filter, Pageable pageable) {
        Specification<Card> spec = filter.toSpecification();
        return cardRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Card> getAllMyCards(CardFilter filter, Pageable pageable) {
        Specification<Card> spec = filter.toSpecification();
        return cardRepository.findAll(spec, pageable);
    }

    @Override
    public CardDto getOne(Long id) {
        Optional<Card> cardOptional = cardRepository.findById(id);
        return cardMapper.toCardDto(cardOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    @Override
    public CardDto create(CardDto dto) {
        User owner = userRepository.findByPhoneNumber(dto.ownerPhoneNumber()).orElseThrow(() ->
                new UserNotFoundException("Пользователь с номером " + dto.ownerPhoneNumber() + " не найден"));

        Card card = cardMapper.toEntity(dto, owner);
        card.setLast4(card.getCardNumber().substring(card.getCardNumber().length() - 4));
        Card resultCard = cardRepository.save(card);

        return cardMapper.toCardDto(resultCard);
    }

    @Override
    public CardDto patch(Long id, JsonNode patchNode) {
        Card card = cardRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        if (patchNode.has(CARD_STATUS)) {
            card.setCardStatus(CardStatus.valueOf(patchNode.get(CARD_STATUS).asText()));
        }

        Card resultCard = cardRepository.save(card);
        return cardMapper.toCardDto(resultCard);
    }

    @Override
    public List<Long> patchMany(List<Long> ids, JsonNode patchNode) {
        Collection<Card> cards = cardRepository.findAllById(ids);

        for (Card card : cards) {
            if (patchNode.has(CARD_STATUS)) {
                card.setCardStatus(CardStatus.valueOf(patchNode.get(CARD_STATUS).asText()));
            }
        }

        List<Card> resultCards = cardRepository.saveAll(cards);
        return resultCards.stream()
                .map(Card::getId)
                .toList();
    }

    @Override
    public CardDto delete(Long id) {
        Card card = cardRepository.findById(id).orElse(null);
        if (card != null) {
            cardRepository.delete(card);
        }
        return cardMapper.toCardDto(card);
    }

    @Override
    public void deleteMany(List<Long> ids) {
        cardRepository.deleteAllById(ids);
    }
}
