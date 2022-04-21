package com.y3technologies.masters.dto.apiConfig;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateApiConfigRequest {

    @Size(max = 300)
    private String description;

    @Size(max = 300)
    @NotBlank
    @URL
    private String url;

    @Size(max = 128)
    private String urn;

    private String apiKey;

    private String username;

    private String password;

    @NotNull
    private Boolean isActive;
}
