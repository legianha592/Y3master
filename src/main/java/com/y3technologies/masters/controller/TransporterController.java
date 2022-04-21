package com.y3technologies.masters.controller;

import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.service.TransporterService;
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
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/${api.version.masters}/transporter", consumes = { MediaType.APPLICATION_JSON_VALUE,
        MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class TransporterController extends BaseController{

    private final TransporterService transporterService;

    private final CustomerService customerService;

    @Autowired
    MessagesUtilities messagesUtilities;

    @PostMapping(value = "/create")
    public ResponseEntity create(@RequestBody @Valid TransporterDto dto) {
        //Code or Name cannot be duplicated
        PartnersFilter partnersFilter = new PartnersFilter();
        partnersFilter.setTenantId(getCurrentTenantId());
        partnersFilter.setPartnerCode(dto.getPartnerCode());
        partnersFilter.setPartnerType(AppConstants.PartnerType.TRANSPORTER);

        partnersFilter.setName(dto.getPartnerName());
        partnersFilter.setPartnerCode(null);

        List<Partners> existingPartnerNameList = transporterService.findByFilter(partnersFilter, getCurrentTenantUserId(), false);

        if(!existingPartnerNameList.isEmpty())
            throw new TransactionException("exception.transporter.duplicate");

        dto.setTenantId(getCurrentTenantId());

        dto.getCustomerIdList().forEach(cusId -> {
            //check customerId
            Partners customer = customerService.findById(cusId);
            if (customer == null) {
                throw new TransactionException("exception.customer.invalid");
            }
        });
        Partners model = new Partners();
        try {
            model = transporterService.save(dto);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.transporter.duplicate");
        }
        return ResponseEntity.status(HttpStatus.OK).body(model.getId());
    }

    @PostMapping(value = "/update")
    public ResponseEntity update(@RequestBody @Valid TransporterDto dto) {
        if (dto.getId() == null || transporterService.findById(dto.getId()) == null) {
            throw new TransactionException("exception.transporter.invalid");
        }

        dto.setTenantId(getCurrentTenantId());

        Partners model = new Partners();
        try {
            model = transporterService.save(dto);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.transporter.duplicate");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new TransactionException("exception.transporter.version");
        }
        return ResponseEntity.status(HttpStatus.OK).body(model.getId());
    }

    @GetMapping(value = "/retrieve")
    public ResponseEntity retrieve(@RequestParam("id") Long id) {
        if (id == null || transporterService.findById(id) == null) {
            throw new TransactionException("exception.transporter.invalid");
        }
        CustomerDto model = transporterService.getById(id, getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(model);
    }

    @PostMapping("/findBySearch")
    public ResponseEntity<Page<TransporterDto>> findBySearch(@RequestBody PartnersFilter filter) {
        filter.setTenantId(getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(transporterService.findBySearch(filter, getCurrentTenantUserId(), null));
    }

    @GetMapping(value = "/updateStatus")
    public ResponseEntity updateStatus(@RequestParam("id") Long id,
                                       @RequestParam("status") Boolean status) {
        if (id == null || transporterService.findById(id) == null) {
            throw new TransactionException("exception.transporter.invalid");
        }
        try {
            transporterService.updateStatus(id, status);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionException("exception.transporter.invalid");
        }
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @GetMapping("/findByFilter")
    public ResponseEntity<List<Partners>> findByFilter(PartnersFilter filter) {
        if(filter.getTenantId() == null) filter.setTenantId(getCurrentTenantId());
        List<Partners> partnersList = transporterService.findByFilter(filter, getCurrentTenantUserId(), true);
        List<Partners> list=
                partnersList.stream()
                        .peek (partners -> partners.setPartnerTypeIds(partners.getPartnerTypes().stream()
                                .map(partnerType -> partnerType.getLookup().getId())
                                .collect(Collectors.toList())))
                        .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping (value = "/uploadFiles")
    public ResponseEntity uploadFile(@RequestParam ("file") MultipartFile file)
    {
        ExcelResponseMessage responseMessage = transporterService.uploadExcel(file, getCurrentTenantId(), getCurrentTenantUserId());
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @PostMapping (value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response, @RequestBody PartnersFilter filter) {
        if (filter == null){
            filter = new PartnersFilter();
        }
        if(Objects.nonNull(filter.getTenantId())) filter.setTenantId(getCurrentTenantId());
        transporterService.downloadExcel(filter, response, getCurrentTenantId(), getCurrentTenantUserId());
    }

    @GetMapping(value = "/findByName")
    public Partners getTransporterByName(@RequestParam("tenantId") Long tenantId, @RequestParam("transporterName") String transporterName) {
        return transporterService.findByName(tenantId, transporterName);
    }

    @GetMapping("/getDropdownList/transporter")
    public ResponseEntity<Page<DropdownPartnerDto>> getTransporterDropdownList (
            @RequestParam(defaultValue = "") String term,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(transporterService.findByTransporterName(getCurrentTenantId(), getCurrentTenantUserId(), term, PageRequest.of(pageNo, pageSize)));
    }
}
