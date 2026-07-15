package com.quan.diabetes.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SearchUtil {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private SearchUtil() {}

    public static String toUnaccented(String input) {
        if (input == null) {
            return "";
        }
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    public static boolean matches(String target, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        if (target == null) {
            return false;
        }
        String normalizedTarget = toUnaccented(target);
        String normalizedKeyword = toUnaccented(keyword);
        return normalizedTarget.contains(normalizedKeyword);
    }
}
