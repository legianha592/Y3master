package com.y3technologies.masters.controller;

import com.y3technologies.masters.client.AddrClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.dto.CreateLocationDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.filter.LocationFilter;
import com.y3technologies.masters.dto.table.LocationTableDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.service.CommonTagService;
import com.y3technologies.masters.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Shao Hui
 */

@RestController
@RequestMapping("/${api.version.masters}/location")
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class LocationController extends BaseTableController<LocationTableDto, Location> {

    @Autowired
    private LocationService locationService;

    @Autowired
    private AddrClient addrClient;

    @Autowired
    private AddrContactClient addrContactClient;

    @Autowired
    private CommonTagService commonTagService;

    @Autowired
    private CommonTagRepository commonTagRepository;

    @PostMapping(value = "/create")
    public @ResponseBody
    ResponseEntity create(@RequestBody @Valid CreateLocationDto createLocationDto) {
        Location model = new Location();
        try {
            BeanCopier copier = BeanCopier.create(CreateLocationDto.class, Location.class, false);
            copier.copy(createLocationDto, model, null);

            AddrDTO addrDTO = createLocationDto.getAddr();
            // save address or reuse addressId if exist when any one of the fields entered
            if (null != addrDTO && ObjectUtils.anyNotNull(addrDTO.getState(), addrDTO.getCountryShortName(), addrDTO.getZipCode(), addrDTO.getStreet(), addrDTO.getStreet2())) {
                Optional<AddrDTO> savedAddress = Optional
                        .ofNullable(addrClient.createOrUpdateAddress(mapAddrDtoFromCreateLocation(createLocationDto)));
                savedAddress.ifPresent(value -> model.setAddressId(value.getId()));
            }

            // save lookup if new
            StringBuilder locationTagBuilder = locationService.processLocationTags(createLocationDto, getCurrentTenantId());
            if (locationTagBuilder.length() > 0) {
                model.setLocationTag(locationTagBuilder.toString());
            } else {
                model.setLocationTag(null);
            }

            if (model.getLocCode() == null) {
                model.setLocCode("");
            }

            locationService.save(model);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.location.duplicate");
        }
        return ResponseEntity.status(HttpStatus.OK).body(model);
    }

    @PostMapping(value = "/update")
    public @ResponseBody
    ResponseEntity update(@RequestBody @Valid CreateLocationDto updateLocationDto) {
        if (updateLocationDto.getId() == null || locationService.getById(updateLocationDto.getId()) == null) {
            throw new TransactionException("exception.location.invalid");
        }

        Location model = locationService.getById(updateLocationDto.getId());
        model.setActiveInd(updateLocationDto.getActiveInd());
        model.setLocDesc(updateLocationDto.getLocDesc());
        model.setLocCode(updateLocationDto.getLocCode() != null ? updateLocationDto.getLocCode() : "");

        AddrDTO addrDTO = updateLocationDto.getAddr();

        // save address or reuse addressId if exist
        if (null != addrDTO && ObjectUtils.anyNotNull(addrDTO.getState(), addrDTO.getCountryShortName(), addrDTO.getZipCode(), addrDTO.getStreet(), addrDTO.getStreet2())) {
            Optional<AddrDTO> savedAddress = Optional
                    .ofNullable(addrClient.createOrUpdateAddress(mapAddrDtoFromCreateLocation(updateLocationDto)));
            savedAddress.ifPresent(value -> model.setAddressId(value.getId()));
        }

        // save commontag if new
        StringBuilder commontagBuilder = locationService.processLocationTags(updateLocationDto, getCurrentTenantId());
        if (commontagBuilder.length() > 0) {
            model.setLocationTag(commontagBuilder.toString());
        } else {
            model.setLocationTag(null);
        }

        try {
            locationService.save(model);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionException("exception.location.duplicate");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new TransactionException("exception.location.version");
        }
        return ResponseEntity.status(HttpStatus.OK).body(model);
    }

    @PostMapping("/findBySearch")
    public ResponseEntity<Page<LocationDto>> findBySearch(@RequestBody LocationFilter filter) {
        filter.setTenantId(getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(locationService.findBySearch(filter, getCurrentTenantUserId()));
    }

    @GetMapping(value = "/retrieve")
    public @ResponseBody
    ResponseEntity retrieve(@RequestParam("id") Long id) {
        Location location = locationService.getById(id);
        CreateLocationDto locationDto;
        if (location != null) {
            locationDto = new CreateLocationDto();
            final BeanCopier copier = BeanCopier.create(Location.class, CreateLocationDto.class, false);
            copier.copy(location, locationDto, null);

            Long addressId = location.getAddressId();

            AddrDTO address = null;
            if (addressId != null) {
                try {
                    Optional<AddrDTO> existingAddress = Optional.ofNullable(addrClient.getAddress(addressId));
                    if (existingAddress.isPresent())
                        address = existingAddress.get();
                } catch (Exception exception) {
                    throw exception;
                }
            }
            List<CommonTag> existingLocationTagList = new ArrayList<>();

            Optional.ofNullable(location.getlocationTagIds())
                    .orElseGet(Collections::emptyList).stream().forEach(i -> {
                existingLocationTagList.add(commonTagService.findById(i));
            });

            StringBuilder tagSb = new StringBuilder();
            StringBuilder idSb = new StringBuilder();
            existingLocationTagList.forEach(locationTag -> {
                if (tagSb.length() != 0) {
                    tagSb.append("|");
                    idSb.append("|");
                }
                tagSb.append(locationTag.getTag());
                idSb.append(locationTag.getId());
            });
            locationDto.setLocationTag(tagSb.toString());
            locationDto.setLocationTagId(idSb.toString());
            locationDto.setLocationTags(existingLocationTagList);
            locationDto.setAddr(address);
        } else {
            throw new TransactionException("exception.location.invalid");
        }

        return ResponseEntity.status(HttpStatus.OK).body(locationDto);
    }

    @GetMapping(value = "/updateStatus")
    public @ResponseBody
    ResponseEntity updateStatus(@RequestParam("id") Long id,
                                @RequestParam("status") Boolean status) {
        if (id == null || locationService.getById(id) == null) {
            throw new TransactionException("exception.location.invalid");
        }
        locationService.updateStatus(id, status);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @PostMapping("/findOrCreate")
    public ResponseEntity<Location> findOrCreateLocation(@RequestBody @Valid LocationDto locationDto) {
        return ResponseEntity.status(HttpStatus.OK).body(locationService.findOrCreateLocation(locationDto));
    }

    private UpdateAddrDTO mapAddrDto(LocationDto createLocationDto) {
        UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();

        BeanCopier copier = BeanCopier.create(LocationDto.class, UpdateAddrDTO.class, false);
        copier.copy(createLocationDto, updateAddrDTO, null);

        return updateAddrDTO;
    }

    private UpdateAddrDTO mapAddrDtoFromCreateLocation(CreateLocationDto createLocationDto) {
        UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();

        BeanCopier copier = BeanCopier.create(AddrDTO.class, UpdateAddrDTO.class, false);
        copier.copy(createLocationDto.getAddr(), updateAddrDTO, null);

        return updateAddrDTO;
    }

    private AddrContactDTO mapAddrContactDto(LocationDto createLocationDto) {
        AddrContactDTO locationAddrContactDTO = new AddrContactDTO();

        BeanCopier copier = BeanCopier.create(LocationDto.class, AddrContactDTO.class, false);
        copier.copy(createLocationDto, locationAddrContactDTO, null);
        return locationAddrContactDTO;
    }

    @Override
    public LocationTableDto mapTableDto(Location data) {
        LocationTableDto tableDto = new LocationTableDto();
        BeanCopier copier = BeanCopier.create(Location.class, LocationTableDto.class, false);
        copier.copy(data, tableDto, null);
        return tableDto;
    }

    @GetMapping("/findAllLocationsByTenant")
    public ResponseEntity<List<LocationDto>> findAllLocationsByTenant(@RequestParam("tenantId") Long tenantId) {

        return ResponseEntity.status(HttpStatus.OK).body(locationService.findAllByTenantId(tenantId));
    }

    @PostMapping("/createOrFind")
    public ResponseEntity<Location> createOrFind(@RequestBody Location location) {

        return ResponseEntity.status(HttpStatus.OK).body(locationService.createOrFind(location));
    }

    @GetMapping("/findByFilter")
    public ResponseEntity<List<LocationDto>> findByFilter(LocationFilter filter) {
        if (filter.getTenantId() == null) filter.setTenantId(getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(locationService.findByFilter(filter));
    }

    @GetMapping("/findLocationByCode")
    public LocationDto findLocationByCode(String locCode) {
        return locationService.findLocationByCode(getCurrentTenantId(), locCode);
    }

    @PostMapping(value = "/uploadFiles")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        ExcelResponseMessage excelResponseMessage =locationService.uploadExcel(file, getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.OK).body(excelResponseMessage);
    }

    @PostMapping(value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response, @RequestBody LocationFilter filter) {
        if (filter == null){
            filter = new LocationFilter();
        }
        filter.setTenantId(getCurrentTenantId());
        filter.setPageSize(Integer.MAX_VALUE);
        locationService.downloadExcel(filter, response, getCurrentTenantUserId());
    }

	@PostMapping("/createOrUpdateMultiple")
	public ResponseEntity<List<LocationDto>> createOrUpdateMultipleLocations(@RequestBody List<LocationDto> locationDtoList) {
		return ResponseEntity.ok().body(locationService.createOrUpdateMultiple(locationDtoList));
	}
}
