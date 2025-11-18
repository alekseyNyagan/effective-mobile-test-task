package com.example.bankcards.util;

public class CardMaskUtil {

    private CardMaskUtil() {}

    public static String maskPan(String pan) {
        if (pan == null) return null;
        String digits = pan.replaceAll("\\s+", "");
        if (digits.length() < 4) {
            return "****";
        }
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }
}
