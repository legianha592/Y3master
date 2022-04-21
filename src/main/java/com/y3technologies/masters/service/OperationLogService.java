package com.y3technologies.masters.service;

import com.y3technologies.masters.model.OperationLog.Operation;

public interface OperationLogService {
	
	void log(Operation operation, String content, String className, String searchKey);

	void log(Boolean flag, Object model, String className, String searchKey);

	void log(Operation updateStatus, Boolean status, String simpleName, String string);
}
