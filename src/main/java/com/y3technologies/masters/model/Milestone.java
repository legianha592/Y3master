package com.y3technologies.masters.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

/**
 * Milestone
 *
 * @author Su Xia
 * @since 2019/11/18
 */
@Entity
@Table(name = "MILESTONE", uniqueConstraints = {
		@UniqueConstraint(name = "UNQ_Milestone_1", columnNames = { "TENANT_ID", "MILESTONE_CODE" }) })
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_MILESTONE", allocationSize = 1)
@Getter
@Setter
public class Milestone extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@Column(name = "TENANT_ID")
	private Long tenantId;

	@Column(name = "MILESTONE_CODE")
	@NotEmpty(message = "milestoneCode {notBlank.message}")
	private String milestoneCode;

	@Column(name = "MILESTONE_DESCRIPTION")
	@NotEmpty(message = "milestoneDescription {notBlank.message}")
	private String milestoneDescription;

	// tag1|tag2|tag3
	@Column(name = "MILESTONE_CATEGORY")
	private String milestoneCategory;

	// lookup
	@Column(name = "MILESTONE_GROUP")
	private String milestoneGroup;

	@Column(name = "CUSTOMER_DESCRIPTION")
	private String customerDescription;

	@Column(name = "IS_INTERNAL")
	private Boolean isInternal = Boolean.TRUE;

	@Column(name = "SEQUENCE")
	private Integer sequence;

	@Column(name = "IS_DEFAULT")
	private Boolean isDefault = Boolean.FALSE;

	@JsonBackReference
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "milestone", orphanRemoval = true, cascade = CascadeType.ALL)
	@ToString.Exclude
	private Set<MilestoneStatusConfig> milestoneStatusConfigList = new HashSet<MilestoneStatusConfig>();

	public void addMilestoneStatusConfig(MilestoneStatusConfig milestoneStatusConfig) {
		milestoneStatusConfig.setMilestone(this);
		this.milestoneStatusConfigList.add(milestoneStatusConfig);
	}

	@Transient
	private String external;

	public void setExternal(Boolean isInternal) {
		if (isInternal == null){
			this.external = "";
		}
		else if (isInternal){
			this.external = "No";
		}
		else if (!isInternal){
			this.external = "Yes";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customerDescription == null) ? 0 : customerDescription.hashCode());
		result = prime * result + ((isInternal == null) ? 0 : isInternal.hashCode());
		result = prime * result + ((milestoneCategory == null) ? 0 : milestoneCategory.hashCode());
		result = prime * result + ((milestoneCode == null) ? 0 : milestoneCode.hashCode());
		result = prime * result + ((milestoneDescription == null) ? 0 : milestoneDescription.hashCode());
		result = prime * result + ((milestoneGroup == null) ? 0 : milestoneGroup.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		return result;
	}
}