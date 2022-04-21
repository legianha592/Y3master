package com.y3technologies.masters.controller;

import com.y3technologies.masters.dto.MilestoneConfiguredStatusDTO;
import com.y3technologies.masters.dto.MilestoneDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.MilestoneFilter;
import com.y3technologies.masters.dto.table.MilestoneTableDto;
import com.y3technologies.masters.exception.BindingResultException;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Milestone;
import com.y3technologies.masters.service.MilestoneService;
import com.y3technologies.masters.util.BeanCopierFactory;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.SortingUtils;
import com.y3technologies.masters.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Milestone Controller
 *
 * @author suxia
 */

@RestController
@RequestMapping("/${api.version.masters}/milestone")
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class MilestoneController extends BaseTableController<MilestoneTableDto, Milestone> {

    @Autowired
    MessagesUtilities messagesUtilities;
    @Autowired
    private MilestoneService milestoneService;

//	@RequestMapping(value = "/create", method = RequestMethod.POST)
//	public @ResponseBody ResponseEntity create(@RequestBody @Valid Milestone model) {
//		try {
//			milestoneService.save(model);
//		} catch (DataIntegrityViolationException e) {
//			throw new TransactionException("exception.milestone.duplicate");
//		}
//		return ResponseEntity.status(HttpStatus.OK).body(model.getId());
//	}

//	@RequestMapping(value = "/update", method = RequestMethod.POST)
//	public @ResponseBody ResponseEntity update(@RequestBody @Valid Milestone model) {
//		if (model.getId() == null || !milestoneService.existsById(model.getId())) {
//			throw new TransactionException("exception.milestone.invalid");
//		}
//		try {
//			milestoneService.save(model);
//		} catch (DataIntegrityViolationException e) {
//			throw new TransactionException("exception.milestone.duplicate");
//		} catch (ObjectOptimisticLockingFailureException e) {
//			throw new TransactionException("exception.milestone.version");
//		}
//		return ResponseEntity.status(HttpStatus.OK).body(model);
//	}

//	@RequestMapping(value = "/retrieve", method = RequestMethod.GET)
//	public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
//		Milestone model = milestoneService.getById(id);
//		return ResponseEntity.status(HttpStatus.OK).body(model);
//	}

    @PostMapping("/create")
    public ResponseEntity<Milestone> create(@RequestBody @Valid MilestoneDTO milestoneDTO,
                                            BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            throw new BindingResultException(bindingResult);

        return ResponseEntity.status(HttpStatus.OK).body(milestoneService.createOrUpdate(milestoneDTO));
    }

    @PutMapping("/update")
    public ResponseEntity<Milestone> update(@RequestBody @Valid MilestoneDTO milestoneDTO,
                                            BindingResult bindingResult) {

        if (!ValidationUtils.Number.isValidId(milestoneDTO.getId())
                || !milestoneService.existsById(milestoneDTO.getId()))
            throw new TransactionException("exception.milestone.invalid");

        if (bindingResult.hasErrors())
            throw new BindingResultException(bindingResult);

        return ResponseEntity.status(HttpStatus.OK).body(milestoneService.createOrUpdate(milestoneDTO));
    }

    @GetMapping("/retrieve")
    public ResponseEntity<MilestoneDTO> retrieve(@RequestParam("id") Long id) {

        if (!ValidationUtils.Number.isValidId(id))
            throw new TransactionException("invalid.milestone.id");

        return ResponseEntity.status(HttpStatus.OK).body(milestoneService.findMilestoneDTOById(id));
    }

    @GetMapping(value = "/listByParam")
    public @ResponseBody
    ResponseEntity listByParam(@RequestBody Milestone model) {
        List<Milestone> list = milestoneService.listByParam(model);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping(value = "/query")
    public @ResponseBody
    ResponseEntity loadByAjax(@RequestBody DataTablesInput input) {
        SortingUtils.addDefaultSortExtra(input);
        DataTablesOutput<Milestone> table = milestoneService.query(input);
        DataTablesOutput<MilestoneTableDto> tableDto = convertToTableDto(table);
        return ResponseEntity.status(HttpStatus.OK).body(tableDto);
    }

    @GetMapping(value = "/updateStatus")
    public @ResponseBody
    ResponseEntity updateStatus(@RequestParam("id") Long id,
                                @RequestParam("status") Boolean status) {
        try {
            milestoneService.updateStatus(id, status);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionException("exception.milestone.invalid");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new TransactionException("exception.milestone.version");
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @Override
    public MilestoneTableDto mapTableDto(Milestone data) {
        MilestoneTableDto tableDto = new MilestoneTableDto();
        BeanCopier copier = BeanCopier.create(Milestone.class, MilestoneTableDto.class, false);
        copier.copy(data, tableDto, null);
        tableDto.setMilestoneCodeDescription(
                messagesUtilities.getResourceMessage(data.getMilestoneCode(), LocaleContextHolder.getLocale()));
        return tableDto;
    }

    @GetMapping("/findByFilter")
    public ResponseEntity<List<MilestoneDTO>> findByFilter(MilestoneFilter filter) {
        List<Milestone> milestoneList = milestoneService.findByFilter(filter);

        List<MilestoneDTO> milestoneDTOList = milestoneList.stream().map(milestone -> {
            MilestoneDTO milestoneDTO = new MilestoneDTO();
            BeanCopierFactory.Milestone_MilestoneDto_copier.copy(milestone, milestoneDTO, null);
            milestoneDTO.setMilestoneCodeDescription(
                    messagesUtilities.getResourceMessage(milestone.getMilestoneCode(), LocaleContextHolder.getLocale()));
            return milestoneDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(milestoneDTOList);
    }

    @GetMapping("/getMilestoneConfiguredStatusByFilter")
    public ResponseEntity<List<MilestoneConfiguredStatusDTO>> getMilestoneConfiguredStatusByFilter(MilestoneFilter filter) {
        return ResponseEntity.status(HttpStatus.OK).body(milestoneService.getMilestoneConfiguredStatusByFilter(filter));
    }

    @PutMapping(value = "/addMilestonesForNewUser")
    public @ResponseBody
    ResponseEntity addMilestonesForNewUser(@RequestParam("tenantId") Long tenantId) {
        if (null == tenantId) {
            throw new TransactionException("exception.milestone.tenant.id.null");
        }
        milestoneService.populateSystemMilestoneForUser(tenantId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/retrieveByMilestoneCodeAndMilestoneStatus")
    public ResponseEntity<String> retrieveByMilestoneCodeAndMilestoneStatus(@RequestParam("milestoneCode") String milestoneCode, @RequestParam("milestoneStatus") String milestoneStatus) {

        return ResponseEntity.status(HttpStatus.OK).body(milestoneService.findByMilestoneCodeAndMilestoneStatusAndTenantId(milestoneCode, milestoneStatus, getCurrentTenantId()));
    }

    @PostMapping(value = "/uploadFiles")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        ExcelResponseMessage excelResponseMessage = milestoneService.uploadExcel(file, getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
    }

    @PostMapping(value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response, @RequestBody MilestoneFilter filter){
        milestoneService.downloadExcel(response, getCurrentTenantId(), filter);
    }
}
