package com.y3technologies.masters.controller;

import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.UomSettingDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.UomSettingFilter;
import com.y3technologies.masters.dto.table.UomSettingTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.UomSetting;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.UomSettingService;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * Partners Controller
 * 
 * @author suxia
 * 
 */
@RestController
@RequestMapping("/${api.version.masters}/uomsetting")
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class UomSettingController extends BaseTableController<UomSettingTableDto, UomSetting>{

	private final UomSettingService uomSettingService;

	@Autowired
	private ExcelClient excelClient;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private LookupService lookupService;

	@PostMapping(value = "/create")
	public @ResponseBody ResponseEntity create(@RequestBody @Valid UomSetting model) {
		try {
			uomSettingService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.uomSetting.duplicate");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
	}
	
	@PostMapping(value = "/update")
	public @ResponseBody ResponseEntity update(@RequestBody @Valid UomSetting model) {
		if(model.getId() == null || uomSettingService.getById(model.getId()) == null) {
			throw new TransactionException("exception.uomSetting.invalid");
		}
		try {
			uomSettingService.save(model);
		} catch (DataIntegrityViolationException e) {
			throw new TransactionException("exception.uomSetting.duplicate");
		} catch (ObjectOptimisticLockingFailureException  e) {
			throw new TransactionException("exception.uomSetting.version");
		}
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@GetMapping(value = "/retrieve")
	public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
		UomSettingDto model = uomSettingService.getById(id);
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}

	@GetMapping(value = "/getAllActiveUom")
	public @ResponseBody ResponseEntity getAllActiveUom(@RequestParam(required = false) String uomGroup) {
		List<UomSettingDto> model = uomSettingService.getAllActiveUOM(uomGroup);
		return ResponseEntity.status(HttpStatus.OK).body(model);
	}
	
	@PostMapping(value = "/query")
    public @ResponseBody ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
		SortingUtils.addDefaultSortExtra(input);
		//If sort by uomGroup which will display lookup instead then findAll without paging and order and do it later
		int length = input.getLength();
		int start = input.getStart();
		List<Order> tempOrders = input.getOrder();
		List<Order> orders = new ArrayList<>(input.getOrder());
		Boolean specialSort = false;
		for (int i = 0; i < tempOrders.size(); i++){
			Order order = tempOrders.get(i);
			if (order.getColumn().equals(2)){//2 is the index of the uomGroup column
				input.setLength(-1); // set length = -1 to get all data
				tempOrders.remove(order);
				specialSort = true;
			}
		}

		DataTablesOutput<UomSetting> table = uomSettingService.query(input);
		DataTablesOutput<UomSettingTableDto> tableDto = convertToTableDto(table);
		List<UomSettingTableDto> uomSettingTableDtoList = tableDto.getData();
		List<String> lookUpCodes = uomSettingTableDtoList.stream().map(UomSettingTableDto::getUomGroup).distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Collections.singletonList(AppConstants.LookupType.UOM_GROUP);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, lookUpCodes);
		uomSettingTableDtoList.forEach(el -> el.setUomGroup(
				lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.UOM_GROUP, el.getUomGroup(), mapLookup)
		));
		tableDto.setData(uomSettingTableDtoList);
		if (specialSort) {
			uomSettingTableDtoList = tableDto.getData();
			customSort(orders, uomSettingTableDtoList);
			List<UomSettingTableDto> sortedEquipmentTableDtoList = uomSettingTableDtoList.subList(start, Math.min(length + start, uomSettingTableDtoList.size()));
			tableDto.setData(sortedEquipmentTableDtoList);
			tableDto.setRecordsFiltered(CollectionUtils.size(uomSettingTableDtoList));
		}
		return ResponseEntity.status(HttpStatus.OK).body(tableDto);
	}

	public void customSort(List<Order> orders, List<UomSettingTableDto> uomSettingTableDtoList) {
		for (Order order: orders){
			Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(order.getDir().equals("asc"));
			//2 is the index of the uomGroup column
			if (order.getColumn().equals(2)){
				uomSettingTableDtoList.sort(SortingUtils.addDefaultSortExtraBaseTableDto(
						Comparator.comparing(UomSettingTableDto::getUomGroup, sortOrder)
				));
			}
		}
	}
	
	@GetMapping(value = "/updateStatus")
	public @ResponseBody ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("status") Boolean status) {
		try {
			uomSettingService.updateStatus(id, status);
		} catch (EmptyResultDataAccessException e) {
			throw new TransactionException("exception.uomSetting.invalid");
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@GetMapping(value = "/getByUomCode")
	public ResponseEntity<UomSettingDto> getByUomCode(@RequestParam("uomCode") String uomCode) {
		UomSettingDto result = uomSettingService.getByUomCode(uomCode);
		return ResponseEntity.ok(result);
	}

	@GetMapping(value = "/getByUomGroup")
	public ResponseEntity getByUomGroup(@RequestParam("uomGroup") String uomGroup) {
		List<UomSettingDto> result = uomSettingService.getByUomGroup(uomGroup);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@Override
	public UomSettingTableDto mapTableDto(UomSetting data) {
		UomSettingTableDto tableDto = new UomSettingTableDto();
		BeanCopier copier = BeanCopier.create(UomSetting.class, UomSettingTableDto.class, false);
		copier.copy(data,tableDto , null);
		return tableDto;
	}

	@GetMapping("/findByFilter")
	public ResponseEntity<List<UomSetting>> findByFilter(UomSettingFilter filter) {
		return ResponseEntity.status(HttpStatus.OK).body(uomSettingService.findByFilter(filter));
	}

	@GetMapping("/getUomSettingDtoByUom")
	public ResponseEntity<List<UomSettingDto>> getUomSettingDtoByUom(@RequestParam("uom") String uom) {
		return ResponseEntity.status(HttpStatus.OK).body(uomSettingService.getUomSettingDtoByUom(uom));
	}

	@PostMapping(value = "/uploadFiles")
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
		ExcelResponseMessage responseMessage = uomSettingService.uploadExcel(file);
		return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
	}

	@PostMapping(value = "/downloadExcel")
	public void downloadExcel(HttpServletResponse response, @RequestBody UomSettingFilter filter) {
		List<UomSetting> dataList = uomSettingService.getAll(filter);
		List<String> uomGroups = dataList.stream().map(UomSetting::getUomGroup).distinct().collect(Collectors.toList());
		List<String> lookUpTypes = Collections.singletonList(AppConstants.LookupType.UOM_GROUP);
		Map<String, Map<String, String>> mapLookup = lookupService.findLookupByTypeAndCode(lookUpTypes, uomGroups);
		dataList.forEach(uomSetting -> uomSetting.setUomGroup(
				lookupService.getLookupDescriptionFromLookupCode(AppConstants.LookupType.UOM_GROUP, uomSetting.getUomGroup(), mapLookup)
		));
		this.sortByMultiFields(filter.getSort(), dataList);
		List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_EXPORT);
		excelUtils.exportExcel(response, dataList, UomSetting.class, template);
	}

	private void sortByMultiFields(List<Sort.Order> lstSort, List<UomSetting> uomSettings) {
		Comparator<UomSetting> comparator = Comparator.comparing(UomSetting::getUom, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		if (lstSort.isEmpty()) {
			uomSettings.sort(comparator);
			return;
		}
		Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.UOM_UOM)) {
			comparator = Comparator.comparing(UomSetting::getUom, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.UOM_GROUP)) {
			comparator = Comparator.comparing(UomSetting::getUomGroup, sortOrder);
		}
		if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.UOM_DESCRIPTION)) {
			comparator = Comparator.comparing(UomSetting::getDescription, sortOrder);
		}
		if (Objects.equals(1, lstSort.size())) {
			comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
			uomSettings.sort(comparator);
			return;
		}
		for (int i = 1; i < lstSort.size(); i++) {
			sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.UOM_UOM)) {
				comparator = comparator.thenComparing(UomSetting::getUom, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.UOM_GROUP)) {
				comparator = comparator.thenComparing(UomSetting::getUomGroup, sortOrder);
			}
			if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.UOM_DESCRIPTION)) {
				comparator = comparator.thenComparing(UomSetting::getDescription, sortOrder);
			}
		}
		comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
		uomSettings.sort(comparator);
	}
}
