package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record CardFilter(String ownerPhoneNumber) {
    public Specification<Card> toSpecification() {
        return ownerPhoneNumberSpec();
    }

    private Specification<Card> ownerPhoneNumberSpec() {
        return ((root, query, cb) -> StringUtils.hasText(ownerPhoneNumber)
                ? cb.equal(root.get("owner").get("phoneNumber"), ownerPhoneNumber)
                : null);
    }
}