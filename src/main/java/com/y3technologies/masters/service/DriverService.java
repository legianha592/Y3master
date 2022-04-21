package com.y3technologies.masters.service;

import java.util.List;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.filter.DriverFilter;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.comm.AddrContact;
import org.springframework.web.multipart.MultipartFile;

public interface DriverService {

	void save(Driver driver);

	Driver getById(Long id);

	DataTablesOutput<Driver> query(DataTablesInput input, Long tenantUserId);

	void updateStatus(Long id, Boolean status);

	void saveAndCreateTenantAccount(Driver driver, AddrContact addrContact, Long userId);

	List<DriverDto> findByTenantId(Long tenantId, Long tenantUserId, DriverFilter filter);

	Driver findByUserEmailAndTenantId(String email, Long tenantId);

	List<DriverDto> findByFilter(DriverFilter filter, Long tenantUserId, boolean isCallFromDriverController);

	List<Driver> findByName(String name);

	int countByCondition (DriverDto driverDto);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId);

    List<Long> findDriverIdsByDriverName(Long tenantId, String driverName);

    Long getNoDriverByTenantId(Long tenantId);

    Long getTransporterIdByDriver(Long driverId);
}
