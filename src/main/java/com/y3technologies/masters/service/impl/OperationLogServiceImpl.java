package com.y3technologies.masters.service.impl;

import javax.transaction.Transactional;

import com.y3technologies.masters.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.y3technologies.masters.model.OperationLog;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.repository.OperationLogRepository;

@Service
@Transactional
public class OperationLogServiceImpl implements OperationLogService {

	@Autowired
	private OperationLogRepository operationLogRepository;

	@Override
	public void log(Operation operation, String content, String className, String searchKey) {
		OperationLog log = new OperationLog();
		log.setOperation(operation);
		log.setModel(className);
		log.setRecord(content);
		log.setSearchKey(searchKey);
		operationLogRepository.save(log);
	}

	@Override
	public void log(Boolean flag, Object model, String className, String searchKey) {
		OperationLog log = new OperationLog();
		Operation operation = flag ? Operation.CREATE : Operation.UPDATE;
		log.setOperation(operation);
		log.setModel(className);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		String content = null;
		try {
			content = mapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
		}
		log.setRecord(content);
		log.setSearchKey(searchKey);
		operationLogRepository.save(log);
	}

	@Override
	public void log(Operation operation, Boolean status, String simpleName, String searchKey) {
		OperationLog log = new OperationLog();
		log.setOperation(operation);
		log.setModel(simpleName);
		log.setRecord(status.toString());
		log.setSearchKey(searchKey);
		operationLogRepository.save(log);
	}
	
}
