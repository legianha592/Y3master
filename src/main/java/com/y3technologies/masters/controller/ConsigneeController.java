package com.y3technologies.masters.controller;

import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.ConsigneeDto;
import com.y3technologies.masters.dto.ConsigneePlaceDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.LocationIdAndPartnerIdDto;
import com.y3technologies.masters.dto.PartnersDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.PartnerLocationFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.dto.filter.PlacesFilter;
import com.y3technologies.masters.dto.filter.TptRequestReportFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.service.ConsigneeService;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.PartnerLocationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sivasankari Subramaniam
 */
@RestController
@RequestMapping(value = "/${api.version.masters}/consignee", consumes = {MediaType.APPLICATION_JSON_VALUE,
        MediaType.ALL_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class ConsigneeController extends BaseController {

    @Autowired
    private final ConsigneeService consigneeService;

    @Autowired
    private final LocationService locationService;

    @Autowired
    private final PartnerLocationService partnerLocationService;

    @Autowired
    private MastersApplicationPropertiesConfig mastersApplicationPropertiesConfig;

    @PostMapping(value = "/create")
    public @ResponseBody ResponseEntity create(@RequestBody @Valid ConsigneeDto consigneeDto) {
        Partners partners = null;
        consigneeDto.setTenantId(ObjectUtils.allNotNull(consigneeDto.getTenantId()) ? consigneeDto.getTenantId() : getCurrentTenantId());
        try {
            partners = consigneeService.save(consigneeDto);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.consignee.duplicate");
        }
        return ResponseEntity.status(HttpStatus.OK).body(partners.getId());
    }

    @PostMapping(value = "/addPlace")
    public @ResponseBody ResponseEntity addPlace(@RequestBody @Valid ConsigneePlaceDto consigneePlaceDto) {

        return ResponseEntity.status(HttpStatus.OK).body(consigneeService.savePlace(consigneePlaceDto));
    }

    @PostMapping(value = "/updatePlace")
    public @ResponseBody ResponseEntity updatePlace(@RequestBody @Valid ConsigneePlaceDto consigneePlaceDto) {

        return ResponseEntity.status(HttpStatus.OK).body(consigneeService.updatePlace(consigneePlaceDto));
    }

    @GetMapping(value = "/retrieve")
    public @ResponseBody ResponseEntity retrieve(@RequestParam("id") Long id) {
        ConsigneeDto consigneeDto = consigneeService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(consigneeDto);
    }

    @GetMapping(value = "/retrieveConsigneeByCustomerId")
    public @ResponseBody ResponseEntity retrieveByCustomerId(@RequestParam("id") Long id) {
        List<ConsigneeDto> consigneeDto = consigneeService.findConsigneeByCustomerId(id, getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(consigneeDto);
    }

    @GetMapping(value = "/retrievePlace")
    public @ResponseBody ResponseEntity retrievePlace(@RequestParam("id") Long id) {
        ConsigneePlaceDto consigneePlaceDto = consigneeService.findByPlaceId(id);
        return ResponseEntity.status(HttpStatus.OK).body(consigneePlaceDto);
    }

    @GetMapping(value = "/retrievePlacesForLocation")
    public @ResponseBody ResponseEntity retrievePlacesForLocation(@RequestParam("id") Long id, @RequestParam("consigneeId") Long consigneeId) {
        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
        partnerLocationFilter.setTenantId(getCurrentTenantId());
        partnerLocationFilter.setConsigneeId(ObjectUtils.allNotNull(consigneeId) ? consigneeId : null);
        partnerLocationFilter.setLocationId(id);
        List<ConsigneePlaceDto> consigneePlaceDtoList = consigneeService.findPlacesByLocationId(partnerLocationFilter);

        return ResponseEntity.status(HttpStatus.OK).body(consigneePlaceDtoList);
    }

    @GetMapping(value = "/retrieveLocationsForConsignee")
    public @ResponseBody ResponseEntity retrieveLocationsForConsignee(@RequestParam("id") Long id) {
        List<LocationDto> locationDtoList = new ArrayList<>();
        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
        partnerLocationFilter.setTenantId(getCurrentTenantId());
        partnerLocationFilter.setConsigneeId(id);
        locationDtoList = consigneeService.findLocationsByConsigneeId(partnerLocationFilter, true, getCurrentTenantUserId(), true);
        return ResponseEntity.status(HttpStatus.OK).body(locationDtoList);
    }

    @PostMapping(value = "/findPartnerLocationByLocationIdAndPartnerId")
    public ResponseEntity<List<PartnerLocation>> findPartnerLocationByLocationIdAndPartnerId(@RequestBody List<LocationIdAndPartnerIdDto> locationIdAndPartnerIdDtoList) {
        return ResponseEntity.status(HttpStatus.OK).body(partnerLocationService.findByLocationIdAndPartnerId(locationIdAndPartnerIdDtoList));
    }

    @PostMapping("/findBySearch")
    public ResponseEntity<Page<ConsigneeDto>> findBySearch(@RequestBody PartnersFilter filter) {
        filter.setPartnerType(AppConstants.PartnerType.CONSIGNEE);
        filter.setTenantId(getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(consigneeService.findBySearch(filter, getCurrentTenantUserId()));
    }

    @PostMapping(value = "/retrieveLocationsForMultipleConsignee")
    public @ResponseBody ResponseEntity retrieveLocationsForMultipleConsignee(@RequestBody List<Long> ids) {
        Map<Long, List<LocationDto>> result = new HashMap<>();
        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter(getCurrentTenantId(), ids);
        result = consigneeService.findLocationsByMultipleConsigneeId(partnerLocationFilter, true);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping(value = "/update")
    public @ResponseBody ResponseEntity update(@RequestParam("id") Long id, @RequestBody ConsigneeDto consigneeDto) {
        Partners partners = consigneeService.update(id, consigneeDto);
        return ResponseEntity.status(HttpStatus.OK).body(partners);
    }

    @PutMapping(value = "/updateStatus")
    public @ResponseBody ResponseEntity updateStatus(@RequestParam("id") Long id, @RequestParam("isActive") boolean isActive) {
        ConsigneeDto consigneeDto = consigneeService.updateStatus(id, isActive);
        return ResponseEntity.status(HttpStatus.OK).body(consigneeDto);
    }

    @GetMapping(value = "/findByPlacesSearch")
    public ResponseEntity<Page<ConsigneePlaceDto>> findByPlacesSearch(PlacesFilter placesFilter) {
        placesFilter.setTenantId(getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(consigneeService.findByPlacesSearch(placesFilter, getCurrentTenantUserId()));
    }

    @PutMapping(value = "/updatePlaceStatus")
    public @ResponseBody ResponseEntity updatePlaceStatus(@RequestParam("id") Long id, @RequestParam("isActive") boolean isActive) {
        Long consigneeId = consigneeService.updatePlaceStatus(id, isActive);
        return ResponseEntity.status(HttpStatus.OK).body(consigneeId);
    }

    @GetMapping("/findByFilter")
    public ResponseEntity<List<Partners>> findByFilter(PartnersFilter filter) {
        if(filter.getTenantId() == null) filter.setTenantId(getCurrentTenantId());

        List<Partners> partnersList = consigneeService.findByFilter(filter);
        List<Partners> list=
                partnersList.stream()
                        .peek (partners -> partners.setPartnerTypeIds(partners.getPartnerTypes().stream()
                                .map(partnerType -> partnerType.getLookup().getId())
                                .collect(Collectors.toList())))
                        .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PutMapping(value = "/updatePlaceDetail")
    public @ResponseBody ResponseEntity updatePlaceDetail(@RequestParam("id") Long id, @RequestParam("isActive") boolean isActive) {
        ConsigneeDto consigneeDto = consigneeService.updateStatus(id, isActive);
        return ResponseEntity.status(HttpStatus.OK).body(consigneeDto);
    }

    @GetMapping(value = "/findAllLocations")
    public @ResponseBody ResponseEntity findAllLocations(@RequestParam("id") Long id) {
        List<LocationDto> locationDtoList = new ArrayList<>();
        if(!ObjectUtils.allNotNull(id)) {
            locationDtoList = locationService.findAllByTenantId(getCurrentTenantId());
        } else {
            PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
            partnerLocationFilter.setTenantId(getCurrentTenantId());
            partnerLocationFilter.setConsigneeId(id);
            locationDtoList = consigneeService.findLocationsByConsigneeId(partnerLocationFilter, false, getCurrentTenantUserId(), false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(locationDtoList);
    }

    @PostMapping("/createOrFind")
    public Long createOrFind(@RequestBody @Valid ConsigneeDto consigneeDto) {
        consigneeDto.setTenantId(ObjectUtils.allNotNull(consigneeDto.getTenantId()) ? consigneeDto.getTenantId() : getCurrentTenantId());
        return consigneeService.createOrFind(consigneeDto);
    }

    @PostMapping(value = "/uploadFiles")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        ExcelResponseMessage excelResponseMessage = consigneeService.uploadExcel(file, getCurrentTenantId(), getCurrentTenantUserId());
        return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
    }

    @PostMapping(value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response, @RequestBody PartnersFilter filter) {
        filter.setPartnerType(AppConstants.PartnerType.CONSIGNEE);
        filter.setTenantId(getCurrentTenantId());
        consigneeService.downloadExcel(response, getCurrentTenantId(), getCurrentTenantUserId(), filter);
    }

    @PostMapping("/createOrUpdateMultiple")
    public ResponseEntity<List<PartnersDto>> createOrUpdateMultiple(@RequestBody List<ConsigneeDto> consigneeDtoList) {
        return ResponseEntity.status(HttpStatus.OK).body(consigneeService.createOrUpdateMultiple(consigneeDtoList));
    }
    @GetMapping("/getConsigneeIdsByConsigneeName")
    public ResponseEntity<List<Long>> findConsigneeIdsByConsigneeName(TptRequestReportFilter tptRequestReportFilter) {
        return ResponseEntity.status(HttpStatus.OK).body(
                consigneeService.findConsigneeIdsByConsigneeName(
                        tptRequestReportFilter.getTenantId(),
                        tptRequestReportFilter.getConsignee()
                )
        );
    }

    @GetMapping(value = "/downloadTemplate")
    @ResponseBody
    public void downloadTptRequestUploadTemplate (HttpServletResponse response) {
        String resourceName = mastersApplicationPropertiesConfig.getUploadExcelTemplatePath() + AppConstants.UploadExcelTemplate.CONSIGNEE_SETTING;

        try(InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode("Request_Upload_Template.xlsx", "utf-8"));
            response.flushBuffer();
            IOUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new TransactionException("exception.tptRequest.excelTemplate.not.exist");
        }
    }
}
