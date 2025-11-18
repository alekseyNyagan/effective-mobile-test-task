package com.example.bankcards.converter;

import com.example.bankcards.crypto.CryptoHolder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CardNumberConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return CryptoHolder.get().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return CryptoHolder.get().decrypt(dbData);
    }
}
