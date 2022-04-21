package com.y3technologies.masters.model.apiConfig;

public enum ApiType {
    INCOMING("INCOMING"),
    OUT_GOING("OUTGOING");
    private final String value;

    ApiType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
