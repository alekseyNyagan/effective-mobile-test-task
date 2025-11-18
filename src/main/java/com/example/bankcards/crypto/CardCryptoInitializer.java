package com.example.bankcards.crypto;

import com.example.bankcards.service.CardCryptoService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class CardCryptoInitializer {
    private final CardCryptoService service;

    public CardCryptoInitializer(CardCryptoService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        CryptoHolder.set(service);
    }
}
