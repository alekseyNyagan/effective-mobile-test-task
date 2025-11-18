package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public record CardFilter(List<CardStatus> statuses) {
    public Specification<Card> toSpecification() {
        return statusIn(statuses);
    }

    private Specification<Card> statusIn(List<CardStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            return root.get("cardStatus").in(statuses);
        };
    }
}
