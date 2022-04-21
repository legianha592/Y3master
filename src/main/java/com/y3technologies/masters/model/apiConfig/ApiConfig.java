package com.y3technologies.masters.model.apiConfig;

import com.y3technologies.masters.model.BaseEntity;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "API_CONFIG")
@SequenceGenerator(name = "TABLE_SEQUENCE", sequenceName = "SEQ_API_CONFIG", allocationSize = 1)
@Getter
@Setter
public class ApiConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOOKUP_ID")
    @NotNull
    private Lookup lookup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @NotNull
    private Partners customer;

    @Column(name = "type")
    @NotNull
    private ApiType type;

    @Column(name = "url")
    @NotBlank
    @Size(max = 300, message = "url length must be under 300")
    @URL
    private String url;

    @Column(name = "urn")
    @Size(max = 128, message = "urn length invalid")
    private String urn;

    @Column(name = "description")
    @Size(max = 300, message = "description length must be under 300")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "authen_type")
    private AuthenticationType authenType; // apiKey || username&password

    @Column(name = "api_key")
    @Size(max = 128, message = "your key is too long, try again with maximum of 128 character")
    private String apiKey;

    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
}
