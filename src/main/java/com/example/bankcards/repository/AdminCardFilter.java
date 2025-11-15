package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record AdminCardFilter(String ownerName, String ownerSurname) {
    public Specification<Card> toSpecification() {
        return ownerNameSpec()
                .and(ownerSurnameSpec());
    }

    private Specification<Card> ownerNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(ownerName)
                ? cb.equal(root.get("owner").get("name"), ownerName)
                : null);
    }

    private Specification<Card> ownerSurnameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(ownerSurname)
                ? cb.equal(root.get("owner").get("surname"), ownerSurname)
                : null);
    }
}