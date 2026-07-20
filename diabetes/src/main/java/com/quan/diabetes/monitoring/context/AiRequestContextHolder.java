package com.quan.diabetes.monitoring.context;

public class AiRequestContextHolder {
    private static final ThreadLocal<String> CURRENT_QUESTION = new ThreadLocal<>();
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    public static void setCurrentQuestion(String question) {
        CURRENT_QUESTION.set(question);
    }

    public static String getCurrentQuestion() {
        return CURRENT_QUESTION.get();
    }

    public static void setStartTime(Long timeMs) {
        START_TIME.set(timeMs);
    }

    public static Long getStartTime() {
        return START_TIME.get();
    }

    public static void clear() {
        CURRENT_QUESTION.remove();
        START_TIME.remove();
    }
}
