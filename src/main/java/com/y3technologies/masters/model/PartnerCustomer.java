package com.y3technologies.masters.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "PARTNER_CUSTOMER")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_PARTNER_CUSTOMER", allocationSize = 1)
@Getter
@Setter
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class PartnerCustomer extends BaseEntity{

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "customer_id")
    private Long customerId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((partnerId == null) ? 0 : partnerId.hashCode());
        result = prime * result + ((customerId == null) ? 0 : customerId.hashCode());
        return result;
    }

}
