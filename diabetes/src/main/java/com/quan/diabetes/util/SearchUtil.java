package com.quan.diabetes.util;

public class SearchUtil {
    public static boolean matches(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.toLowerCase().contains(keyword.toLowerCase().trim());
    }

    public static boolean startsWith(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.toLowerCase().startsWith(keyword.toLowerCase().trim());
    }
}

