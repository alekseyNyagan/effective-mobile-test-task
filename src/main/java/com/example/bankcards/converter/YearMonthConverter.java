package com.example.bankcards.converter;

import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        return (yearMonth != null) ? yearMonth.toString() : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return (dbData != null) ? YearMonth.parse(dbData) : null;
    }
}
