
package com.y3technologies.masters.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

/**
 * PartnerTypes
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Entity
@Table(name = "PARTNER_TYPES", uniqueConstraints = {@UniqueConstraint(name = "UNQ_PARTNER_TYPES_1", columnNames = {"PARTNER_TYPE_ID", "PARTNER_ID"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_PARTNER_TYPES", allocationSize = 1)
@Getter
@Setter
public class PartnerTypes extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTNER_ID")
    private Partners partners;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTNER_TYPE_ID")
    private Lookup lookup;

}