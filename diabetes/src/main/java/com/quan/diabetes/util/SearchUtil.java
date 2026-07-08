package com.quan.diabetes.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SearchUtil {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String removeAccents(String src) {
        if (src == null) {
            return null;
        }
        String normalized = Normalizer.normalize(src, Normalizer.Form.NFD);
        String cleared = DIACRITICS.matcher(normalized).replaceAll("");
        return cleared.replace("đ", "d").replace("Đ", "D");
    }

    public static boolean matches(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        String normalizedText = removeAccents(text).toLowerCase();
        String normalizedKeyword = removeAccents(keyword).toLowerCase().trim();
        return normalizedText.contains(normalizedKeyword);
    }

    public static boolean startsWith(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        String normalizedText = removeAccents(text).toLowerCase();
        String normalizedKeyword = removeAccents(keyword).toLowerCase().trim();
        return normalizedText.startsWith(normalizedKeyword);
    }
}

