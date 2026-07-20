package com.quan.diabetes.aiAPI.dto;

public class AiGenerateOptions {
    private String modelName;
    private Double temperature;
    private Double topP;
    private Integer maxOutputTokens;

    public AiGenerateOptions() {
    }

    public AiGenerateOptions(String modelName, Double temperature, Double topP, Integer maxOutputTokens) {
        this.modelName = modelName;
        this.temperature = temperature;
        this.topP = topP;
        this.maxOutputTokens = maxOutputTokens;
    }

    public static AiGenerateOptions defaults(String modelName) {
        return new AiGenerateOptions(modelName, 0.15, 0.9, 1024);
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(Integer maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }
}
