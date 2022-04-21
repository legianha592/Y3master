package com.y3technologies.masters.model.apiConfig;

public enum ApiAuditAction {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String text;

    ApiAuditAction(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
