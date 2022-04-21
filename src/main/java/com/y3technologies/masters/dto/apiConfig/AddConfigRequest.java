package com.y3technologies.masters.dto.apiConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddConfigRequest {

    @NotNull
    private Long apiId; // lookupId

    @NotNull
    private Long customerId; // partnersId

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
