package com.example.bankcards.crypto;

import com.example.bankcards.service.CardCryptoService;

public class CryptoHolder {
    private static CardCryptoService cryptoService;

    public static void set(CardCryptoService service) {
        cryptoService = service;
    }

    public static CardCryptoService get() {
        if (cryptoService == null) {
            throw new IllegalStateException("CryptoService not initialized");
        }
        return cryptoService;
    }
}
