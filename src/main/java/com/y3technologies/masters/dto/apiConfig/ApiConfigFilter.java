package com.y3technologies.masters.dto.apiConfig;

import com.y3technologies.masters.dto.filter.ListingFilter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ApiConfigFilter extends ListingFilter implements Serializable {
    private List<Long> apiId; // lookupId
    private String customerName; // partnersId
    private String apiType;
    private String apiKey;
    private String username;
    private String password;
    private String urn;
}
