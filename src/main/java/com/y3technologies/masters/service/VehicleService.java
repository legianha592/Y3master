package com.y3technologies.masters.service;

import java.util.List;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.y3technologies.masters.dto.VehicleDto;
import com.y3technologies.masters.dto.filter.VehicleFilter;
import com.y3technologies.masters.model.Vehicle;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleService {

	Vehicle save(VehicleDto vehicle);

	VehicleDto getById(Long id);

	DataTablesOutput<Vehicle> query(DataTablesInput input, Long tenantUserId);

	void updateStatus(Long id, Boolean status);

	List<VehicleDto> findByTenantId(Long tenantId, Long tenantUserId, VehicleFilter filter);

	List<Vehicle> findByDefaultDriverIdAndTenantId(Long defaultDriverId, Long tenantId);

	List<Vehicle> findByFilter(VehicleFilter filter);

	int countByCondition (Long tenantId, String vehicleRegNumber);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId, Long currentTenantUserId);

    List<Long> findVehicleIdsByVehiclePlateNo(Long tenantId, String vehiclePlateNo);

    VehicleDto findByVehicleNo(Long tenantId, String vehicleNo);
}
