package com.y3technologies.masters.model;

import java.io.Serializable;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "milestone_status_config", uniqueConstraints = {
		@UniqueConstraint(name = "milestone_status_config_un", columnNames = { "milestone_id", "lookup_id" }) })
public class MilestoneStatusConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY )
	@JoinColumn(name = "milestone_id", nullable = false)
	private Milestone milestone;

	@Column(name = "lookup_id")
	private Long lookupId;

	@Column(name = "tpt_request_status_type_id")
	private Long tptRequestStatusTypeId;

	private Boolean activated;

	@Version
	@Column(name = "VERSION")
	protected Long version;

}
