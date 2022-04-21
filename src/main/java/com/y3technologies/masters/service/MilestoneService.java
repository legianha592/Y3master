package com.y3technologies.masters.service;

import java.util.List;

import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.y3technologies.masters.dto.MilestoneConfiguredStatusDTO;
import com.y3technologies.masters.dto.MilestoneDTO;
import com.y3technologies.masters.dto.filter.MilestoneFilter;
import com.y3technologies.masters.model.Milestone;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface MilestoneService {

	Milestone save(Milestone model);

	boolean existsById(Long id);

	List<Milestone> listByParam(Milestone milestone);

	DataTablesOutput<Milestone> query(DataTablesInput input);

	Milestone getById(Long id);

	void updateStatus(Long id, Boolean status);

	List<Milestone> findByFilter(MilestoneFilter filter);

	Milestone createOrUpdate(MilestoneDTO milestoneDTO);

	MilestoneDTO findMilestoneDTOById(Long id);

	List<MilestoneConfiguredStatusDTO> getMilestoneConfiguredStatusByFilter(MilestoneFilter filter);

	void populateSystemMilestoneForUser(Long tenantId);

	String findByMilestoneCodeAndMilestoneStatusAndTenantId(String milestoneCode, String milestoneStatus, Long tenantId);

	List<Milestone> findByTenantId(Long tenantId, MilestoneFilter filter);

	int countByCondition(String milestoneCode, Long tenantId);

	ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId);

	void downloadExcel(HttpServletResponse response, Long currentTenantId, MilestoneFilter filter);
}
