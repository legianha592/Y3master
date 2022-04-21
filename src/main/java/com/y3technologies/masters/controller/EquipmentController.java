package com.y3technologies.masters.controller;

import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.EquipmentFilter;
import com.y3technologies.masters.dto.table.EquipmentTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Equipment;
import com.y3technologies.masters.service.EquipmentService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.SortingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.mapping.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;

/**
 * Equipment Controller
 * 
 * @author suxia
 * 
 */

@RestController
@RequestMapping("/${api.version.masters}/equipment")
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class EquipmentController extends BaseTableController<EquipmentTableDto, Equipment>{

	@Autowired
	private EquipmentService equipmentService;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private LookupService lookupService;
	
	@PostMapping(value = "/create")
	public @ResponseBody ResponseEntity create(@RequestBody @Valid Equipment model) {
		try {
			equipmentService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.equipment.duplicate");
		} 
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}
	
	@PostMapping(value = "/update")
	public @ResponseBody ResponseEntity update(@RequestBody @Valid Equipment model) {
		if(model.getId() == null || !equipmentService.existsById(model.getId())) {
			throw new TransactionException("exception.equipments.invalid");
		}
		try {
			equipmentService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.equipment.duplicate");
		}catch (ObjectOptimisticLockingFailureException  e) {
			throw new TransactionException("exception.equipment.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/listByParam")
	public @ResponseBody ResponseEntity listByParam(@RequestBody Equipment model) {
		List<Equipment> list = equipmentService.listByParam(model);
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}
	
	@PostMapping(value = "/query")
    public @ResponseBody ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
		SortingUtils.addDefaultSortExtra(input);
		//If sort by equipmentType or unitType which will display lookup instead then findAll without paging and order and do it later
		int length = input.getLength();
		int start = input.getStart();
		List<Order> tempOrders = input.getOrder();
		List<Order> orders = new ArrayList<>(input.getOrder());
		Boolean specialSort = false;
		for (int i = 0; i < tempOrders.size(); i++){
			Order order = tempOrders.get(i);
			if (order.getColumn().equals(3)||order.getColumn().equals(4)){
				input.setLength(-1); // set length = -1 to get all data
				tempOrders.remove(order);
				specialSort = true;
			}
		}
		DataTablesOutput<Equipment> table = equipmentService.query(input);
		DataTablesOutput<EquipmentTableDto> tableDto = convertToTableDto(table);
		List<EquipmentTableDto> equipmentTableDtosList = tableDto.getData();
		List<String> lookupCodes = new ArrayList<>();
		equipmentTableDtosList.forEach(el -> {
			lookupCodes.add(el.getEquipmentType());
			lookupCodes.add(el.getUnitType());
		});
		List<String> lookUpCodesDistinct = lookupCodes.stream().distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Arrays.asList(AppConstants.LookupType.EQUIPMENT_TYPE, AppConstants.LookupType.EQUIPMENT_UNIT_TYPE);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, lookUpCodesDistinct);
		equipmentTableDtosList.forEach(equipment -> {
			equipment.setEquipmentType(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.EQUIPMENT_TYPE, equipment.getEquipmentType(), mapLookup)
			);
			equipment.setUnitType(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.EQUIPMENT_UNIT_TYPE, equipment.getUnitType(), mapLookup)
			);
		});
		tableDto.setData(equipmentTableDtosList);
		if (specialSort) {
			equipmentTableDtosList = tableDto.getData();
			customSort(orders, equipmentTableDtosList);
			List<EquipmentTableDto> sortedEquipmentTableDtoList = equipmentTableDtosList.subList(start, Math.min(length + start, equipmentTableDtosList.size()));
			tableDto.setData(sortedEquipmentTableDtoList);
			tableDto.setRecordsFiltered(CollectionUtils.size(equipmentTableDtosList));
		}
		return ResponseEntity.status(HttpStatus.OK).body(tableDto);
	}

	public void customSort(List<Order> orders, List<EquipmentTableDto> equipmentTableDtoList) {
		for (Order order: orders){
			Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(order.getDir().equals("asc"));
			//3 is the index of the equipmentType column
			if (order.getColumn().equals(3)){
				equipmentTableDtoList.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(EquipmentTableDto::getEquipmentType, sortOrder)
				));
			}
			//4 is the index of the unitType column
			if (order.getColumn().equals(4)){
				equipmentTableDtoList.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(EquipmentTableDto::getUnitType, sortOrder)
				));
			}
		}
	}


	@GetMapping(value = "/retrieve")
	public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
		Equipment model = equipmentService.getById(id);
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/updateStatus")
	public @ResponseBody ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("status") Boolean status) {
		try {
			equipmentService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.equipment.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
		ExcelResponseMessage responseMessage = equipmentService.uploadExcel(file, getCurrentTenantId());
		return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody EquipmentFilter filter){
		List<Equipment> dataList = equipmentService.findByTenantId(getCurrentTenantId(), filter);
		List<String> lookupCodes = new ArrayList<>();
		dataList.forEach(el -> {
			lookupCodes.add(el.getEquipmentType());
			lookupCodes.add(el.getUnitType());
		});
		List<String> lookUpCodesDistinct = lookupCodes.stream().distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Arrays.asList(AppConstants.LookupType.EQUIPMENT_TYPE, AppConstants.LookupType.EQUIPMENT_UNIT_TYPE);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, lookUpCodesDistinct);
		dataList.forEach(equipment -> {
			equipment.setEquipmentType(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.EQUIPMENT_TYPE, equipment.getEquipmentType(), mapLookup)
			);
			equipment.setUnitType(
					lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.EQUIPMENT_UNIT_TYPE, equipment.getUnitType(), mapLookup)
			);
		});
		this.sortByMultiFields(filter.getSort(), dataList);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, Equipment.class, template);
	}

	private void sortByMultiFields(List<Sort.Order> lstSort, List<Equipment> equipmentList) {
		Comparator<Equipment> comparator = Comparator.comparing(Equipment::getUnitAidc1, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		if (lstSort.isEmpty()) {
			equipmentList.sort(comparator);
			return;
		}
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_UNITAIDC1)) {
			comparator = Comparator.comparing((Equipment::getUnitAidc1), sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_UNITAIDC2)) {
			comparator = Comparator.comparing(Equipment::getUnitAidc2, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_TYPE)) {
			comparator = Comparator.comparing(Equipment::getEquipmentType, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_UNIT_TYPE)) {
			comparator = Comparator.comparing(Equipment::getUnitType, sortOrder);
		}

		if (Objects.equals(1, lstSort.size())) {
			comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
			equipmentList.sort(comparator);
			return;
		}
		for (int i = 1; i < lstSort.size(); i++) {
			sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_UNITAIDC1)) {
				comparator = comparator.thenComparing(Equipment::getUnitAidc1, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_UNITAIDC2)) {
				comparator = comparator.thenComparing(Equipment::getUnitAidc2, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_TYPE)) {
				comparator = comparator.thenComparing(Equipment::getEquipmentType, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.EQUIPMENT_UNIT_TYPE)) {
				comparator = comparator.thenComparing(Equipment::getUnitType, sortOrder);
			}
		}
		comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
		equipmentList.sort(comparator);
	}

	@Override
	public EquipmentTableDto mapTableDto(Equipment data) {
		EquipmentTableDto tableDto = new EquipmentTableDto();
		BeanCopier copier = BeanCopier.create(Equipment.class, EquipmentTableDto.class, false);
		copier.copy(data,tableDto , null);
		return tableDto;
	}
}
