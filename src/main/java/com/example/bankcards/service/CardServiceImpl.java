package com.example.bankcards.service;

import com.example.bankcards.repository.CardFilter;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.BlockRequestStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.AdminCardFilter;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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

    private final CardBlockRequestRepository requestRepository;

    private final UserService userService;

    private final ObjectMapper objectMapper;

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
                new UserNotFoundException("User with phone number " + dto.ownerPhoneNumber() + " not found"));

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

    @Override
    public void createBlockRequest(Long cardId) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        User currentUser = userService.getCurrentUser();

        if (!card.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("This card doesn't belong to you");
        }

        if (requestRepository.existsByCardAndStatus(card, BlockRequestStatus.PENDING)) {
            throw new BadRequestException("Request already submitted");
        }

        CardBlockRequest request = new CardBlockRequest();
        request.setCard(card);
        request.setUser(currentUser);

        requestRepository.save(request);
    }

    @Override
    public void approveBlockRequest(Long requestId) {

        CardBlockRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Block request not found"));

        if (request.getStatus() != BlockRequestStatus.PENDING) {
            throw new BadRequestException("This request is already processed");
        }

        request.setStatus(BlockRequestStatus.APPROVED);
        requestRepository.save(request);

        ObjectNode node = objectMapper.createObjectNode();
        node.put(CARD_STATUS, CardStatus.BLOCKED.name());

        patch(request.getCard().getId(), node);
    }

    @Override
    public BigDecimal getBalance(Long cardId) {
        User currentUser = userService.getCurrentUser();

        Card card = cardRepository.findByIdAndUser(cardId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new ForbiddenOperationException("Card is blocked");
        }

        return card.getMoneyAmount();
    }
}
