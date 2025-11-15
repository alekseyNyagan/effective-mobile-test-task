package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record UserFilter(String phoneNumber) {
    public Specification<User> toSpecification() {
        return phoneNumberSpec();
    }

    private Specification<User> phoneNumberSpec() {
        return ((root, query, cb) -> StringUtils.hasText(phoneNumber)
                ? cb.equal(root.get("phoneNumber"), phoneNumber)
                : null);
    }
}