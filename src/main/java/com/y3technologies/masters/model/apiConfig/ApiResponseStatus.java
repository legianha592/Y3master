package com.y3technologies.masters.model.apiConfig;

public enum ApiResponseStatus {
    SUCCESSFUL("SUCCESSFUL"),
    FAILED("FAILED");

    private final String text;

    ApiResponseStatus(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
