package com.y3technologies.masters.model.apiConfig;

import com.y3technologies.masters.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "API_AUDIT_LOG")
@SequenceGenerator(name = "TABLE_SEQUENCE", sequenceName = "SEQ_API_AUDIT_LOG", allocationSize = 1)
@Getter
@Setter
public class ApiAuditLog extends BaseEntity {

    @Column(name = "api_id")
    @NotNull
    private Long apiId;

    @Column(name = "tenant_id")
    @NotNull
    private Long tenantId;

    @Column(name = "audit_user")
    @NotNull
    private Long auditUser;

    @Column(name = "audit_request_dttm")
    @NotNull
    private LocalDateTime auditRequestDateTime;

    @Column(name = "audit_response_dttm")
    @NotNull
    private LocalDateTime auditResponseDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_action")
    @NotNull
    private ApiAuditAction auditAction;

    @Column(name = "api_url")
    @NotNull
    @Size(max = 300, message = "url length must be under 300")
    @URL
    private String apiURL;

    @Column(name = "api_urn")
    @NotNull
    @Size(max = 128, message = "urn length invalid")
    private String apiURN;

    @Column(name = "api_name")
    @NotNull
    private String apiName;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type")
    @NotNull
    private ApiType apiType;

    @Column(name = "customer_name")
    @NotNull
    private String customerName;

    @Column(name = "request_params", columnDefinition = "TEXT")
    @NotNull
    private String requestParams;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    @NotNull
    private String requestHeaders;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_status")
    @NotNull
    private ApiResponseStatus responseStatus;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    @NotNull
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    @NotNull
    private String responsePayload;
}
