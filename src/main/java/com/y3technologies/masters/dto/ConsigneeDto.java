package com.y3technologies.masters.dto;

import com.querydsl.core.annotations.QueryInit;
import com.y3technologies.masters.model.comm.AddrDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * @author Sivasankari Subramaniam
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsigneeDto extends BaseDto {

    private Long tenantId;

    @NotNull(message = "partnerCode {notBlank.message}")
    private String partnerName;

    private String partnerCode;

    @NotNull(message = "customerId {notBlank.message}")
    private Long customerId;

    private String customerName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsigneeDto that = (ConsigneeDto) o;
        return tenantId.equals(that.tenantId) && partnerName.equals(that.partnerName) && Objects.equals(customerId, that.customerId) && customerName.equals(that.customerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, partnerName, customerId, customerName);
    }
}
