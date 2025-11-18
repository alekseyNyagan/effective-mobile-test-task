package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardMaskUtil;


public abstract class CardMapperDelegate implements CardMapper {

    @Override
    public Card toEntity(CardDto cardDto, User owner) {
            Card card = new Card();
            card.setCardNumber(cardDto.cardNumber());
            card.setExpiry(cardDto.expiry());
            card.setMoneyAmount(cardDto.moneyAmount());
            card.setCardStatus(cardDto.cardStatus());
            card.setOwner(owner);
            return card;
    }

    @Override
    public CardDto toCardDto(Card card) {
        return new CardDto(
                CardMaskUtil.maskPan(card.getCardNumber()),
                card.getExpiry(),
                card.getMoneyAmount(),
                card.getCardStatus(),
                card.getOwner().getPhoneNumber()
        );
    }
}
