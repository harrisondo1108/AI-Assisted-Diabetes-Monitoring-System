package com.quan.diabetes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PythonAiResponse {
    private String status;
    private String content;
    private Map<String, Object> tool;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getTool() {
        return tool;
    }

    public void setTool(Map<String, Object> tool) {
        this.tool = tool;
    }
}
