package com.y3technologies.masters.controller;

import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.util.MessagesUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/${api.version.masters}/customer", consumes = { MediaType.APPLICATION_JSON_VALUE,
        MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class CustomerController extends BaseController {

    private final CustomerService customerService;

    @Autowired
    MessagesUtilities messagesUtilities;

    @PostMapping(value = "/create")
    public ResponseEntity create(@RequestBody @Valid CustomerDto dto) {
        //Code or Name cannot be duplicated
        PartnersFilter partnersFilter = new PartnersFilter();
        partnersFilter.setTenantId(getCurrentTenantId());
        partnersFilter.setPartnerCode(dto.getPartnerCode());
        partnersFilter.setIsCreateCustomer(Boolean.TRUE);
        partnersFilter.setPartnerType(AppConstants.PartnerType.CUSTOMER);

        partnersFilter.setName(dto.getPartnerName());
        partnersFilter.setPartnerCode(null);
        List<Partners> existingPartnerNameList = customerService.findByFilter(partnersFilter, getCurrentTenantUserId(), false, null);

        if(!existingPartnerNameList.isEmpty())
            throw new TransactionException("exception.customer.duplicate");

        dto.setTenantId(getCurrentTenantId());

        Partners model = new Partners();
        try {
            model = customerService.save(dto);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.customer.duplicate");
        }
        return ResponseEntity.status(HttpStatus.OK).body(model.getId());
    }

    @PostMapping(value = "/update")
    public ResponseEntity update(@RequestBody @Valid CustomerDto dto) {
        if (dto.getId() == null || customerService.findById(dto.getId()) == null) {
            throw new TransactionException("exception.customer.invalid");
        }

        dto.setTenantId(getCurrentTenantId());

        Partners model = new Partners();
        try {
            model = customerService.save(dto);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.customer.duplicate");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new TransactionException("exception.customer.version");
        }
        return ResponseEntity.status(HttpStatus.OK).body(model.getId());
    }

    @GetMapping(value = "/retrieve")
    public ResponseEntity retrieve(@RequestParam("id") Long id) {
        if (id == null || customerService.findById(id) == null) {
            throw new TransactionException("exception.customer.invalid");
        }
        CustomerDto model = customerService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(model);
    }

    @PostMapping("/findBySearch")
    public ResponseEntity<Page<Partners>> findBySearch(@RequestBody PartnersFilter filter) {
        filter.setTenantId(getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(customerService.findBySearch(filter, getCurrentTenantUserId(), null));
    }

    @GetMapping(value = "/updateStatus")
    public ResponseEntity updateStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Boolean status) {
        if (id == null || customerService.findById(id) == null) {
            throw new TransactionException("exception.customer.invalid");
        }
        try {
            customerService.updateStatus(id, status);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionException("exception.customer.invalid");
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @GetMapping("/findByFilter")
    public ResponseEntity<List<Partners>> findByFilter(PartnersFilter filter) {
        if(filter.getTenantId() == null) filter.setTenantId(getCurrentTenantId());
        List<Partners> partnersList = customerService.findByFilter(filter, getCurrentTenantUserId(), true, null);
        List<Partners> list=
                partnersList.stream()
                        .peek (partners -> partners.setPartnerTypeIds(partners.getPartnerTypes().stream()
                                .map(partnerType -> partnerType.getLookup().getId())
                                .collect(Collectors.toList())))
                        .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping(value = "/uploadFiles")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        ExcelResponseMessage excelResponseMessage = customerService.uploadExcel(file, getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
    }

    @PostMapping (value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response, @RequestBody PartnersFilter filter) {
        if (filter == null){
            filter = new PartnersFilter();
        }
        filter.setTenantId(getCurrentTenantId());
        filter.setPageSize(Integer.MAX_VALUE);
        customerService.downloadExcel(filter, response, getCurrentTenantUserId());
    }

    @GetMapping("/findByName")
    public Partners findByName(@RequestParam("tenantId") Long tenantId, @RequestParam("customerName") String customerName) {
        return customerService.findByName(tenantId, customerName);
    }

    @GetMapping("/getDropdownList/customer")
    public ResponseEntity<Page<DropdownPartnerDto>> getTransporterDropdownList (
            @RequestParam(defaultValue = "") String term,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.findByCustomerName(getCurrentTenantId(), getCurrentTenantUserId(), term, PageRequest.of(pageNo, pageSize)));
    }
}
