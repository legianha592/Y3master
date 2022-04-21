package com.y3technologies.masters.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BaseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	public Long id;

	public Boolean activeInd = Boolean.TRUE;
	protected String createdBy;
	// @JsonFormat(pattern = ConstantVar.DATETIME_FORMATE, locale = "zh", timezone =
	// "GMT+8")
//	@JsonFormat(pattern = ConstantVar.DATETIME_FORMATE, locale = "zh", timezone = "GMT+8")
	protected LocalDateTime createdDate;

	protected String updatedBy;

	// @JsonFormat(pattern = ConstantVar.DATETIME_FORMATE, locale = "zh", timezone =
	// "GMT+8")
//	@JsonFormat(pattern = ConstantVar.DATETIME_FORMATE, locale = "zh", timezone = "GMT+8")
	protected LocalDateTime updatedDate;
	
	protected Long version;

	protected Integer hashcode;

	protected int excelRowPosition;

	public int getExcelRowPosition() {
		return excelRowPosition;
	}

	public void setExcelRowPosition(int excelRowPosition) {
		this.excelRowPosition = excelRowPosition;
	}

	public Integer getHashcode() {
		return hashcode;
	}

	public void setHashcode(Integer hashcode) {
		this.hashcode = hashcode;
	}

	public BaseDto() {
		super();
	}

	public BaseDto(Long id) {
		super();
		this.id = id;
	}

	public BaseDto(Long id, Boolean activeInd, String createdBy, LocalDateTime createdDate, String updatedBy,
			LocalDateTime updatedDate, int excelRowPosition) {
		super();
		this.id = id;
		this.activeInd = activeInd;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.updatedBy = updatedBy;
		this.updatedDate = updatedDate;
		this.excelRowPosition = excelRowPosition;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the activeInd
	 */
	public Boolean getActiveInd() {
		return activeInd;
	}

	/**
	 * @param activeInd the activeInd to set
	 */
	public void setActiveInd(Boolean activeInd) {
		this.activeInd = activeInd;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the createdTimestamp
	 */
	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdTimestamp the createdTimestamp to set
	 */
	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * @return the updatedTimestamp
	 */
	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * @param updatedTimestamp the updatedTimestamp to set
	 */
	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

}
