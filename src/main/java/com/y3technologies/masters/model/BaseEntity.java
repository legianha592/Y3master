package com.y3technologies.masters.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * {@code BaseEntity}
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@EntityListeners({ AuditingEntityListener.class })
@MappedSuperclass
@Data
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "TABLE_SEQ")
	@Column(name = "ID", unique = true, nullable = false)
	protected Long id;

	@Column(name = "HASHCODE")
	@Schema(hidden = true)
	protected Integer hashcode;

	@Column(name = "ACTIVE_IND")
	protected Boolean activeInd = Boolean.TRUE;

	@CreatedBy
	@Schema(hidden = true)
	@Column(name = "CREATED_BY", insertable = true, updatable = false)
	protected String createdBy;

	@CreatedDate
	@Schema(hidden = true)
	@Column(name = "CREATED_DATE", insertable = true, updatable = false)
	protected LocalDateTime createdDate;

	@LastModifiedBy
	@Schema(hidden = true)
	@Column(name = "UPDATED_BY", insertable = false, updatable = true)
	protected String updatedBy;

	@LastModifiedDate
	@Schema(hidden = true)
	@Column(name = "UPDATED_DATE", insertable = false, updatable = true)
	protected LocalDateTime updatedDate;

	@Version
	@Column(name = "VERSION")
	protected Long version;

}