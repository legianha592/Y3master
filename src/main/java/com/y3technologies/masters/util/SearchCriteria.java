package com.y3technologies.masters.util;

import java.io.Serializable;

public class SearchCriteria implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	private String key;

	private String operation;

	private Object value;

	private String connect;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getConnect() {
		return connect;
	}

	public void setConnect(String connect) {
		this.connect = connect;
	}

	public SearchCriteria(String key, String operation, Object value) {
		super();
		this.key = key;
		this.operation = operation;
		this.value = value;
	}

	public SearchCriteria(String key, String operation, Object value, String connect) {
		super();
		this.key = key;
		this.operation = operation;
		this.value = value;
		this.connect = connect;
	}

	public SearchCriteria() {
		super();
	}


}
