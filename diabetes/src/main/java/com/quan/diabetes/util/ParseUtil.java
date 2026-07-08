package com.quan.diabetes.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ParseUtil {
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value);
    }

    public static Boolean parseGender(String value) {
        return isBlank(value) ? null : "1".equals(value);
    }

    public static String parseString(String value){
        return isBlank(value) ? null : value.trim();
    }

    public static Integer parseInteger(String value) {
        return isBlank(value) ? null : Integer.parseInt(value);
    }

    public static BigDecimal parseBigDecimal(String value) {
        return isBlank(value) ? null : new BigDecimal(value);
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 8
                && password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$].*");
    }
}
