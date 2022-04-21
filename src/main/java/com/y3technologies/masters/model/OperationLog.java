package com.y3technologies.masters.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * OperationLog
 *
 * @author Su Xia
 * @since 2019/11/18
 */
@Entity
@Table(name = "OPERATION_LOG")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_OPERATION_LOG", allocationSize = 1)
@Getter
@Setter
public class OperationLog extends BaseEntity{

	@Column(name = "MODEL")
	private String model;
	
	@Column(name = "OPERATION")
	@Enumerated(EnumType.STRING)
	private Operation operation;
	
	@Column(name = "SEARCH_KEY")
	private String searchKey;
	
	@Column(name = "RECORD",columnDefinition="text")
	private String record;

	public static enum Operation {
		CREATE("CREATE"), UPDATE("UPDATE"), DELETE("DELETE"), UPDATE_STATUS("UPDATE_STATUS");
		private final String name;
		Operation(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return this.name;
		}
	}
	
}