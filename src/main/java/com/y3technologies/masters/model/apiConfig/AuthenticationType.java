package com.y3technologies.masters.model.apiConfig;

public enum AuthenticationType {

    API_KEY("api.config.authentype.apikey"),
    USERNAME_PASSWORD("api.config.authentype.usernamepasswd");

    private final String desc;

    AuthenticationType(String desc) {
        this.desc = desc;
    }
}
