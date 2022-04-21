package com.y3technologies.masters.client;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.y3technologies.masters.dto.comm.CityDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.comm.StateDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.filter.AddressFilter;
import com.y3technologies.masters.model.comm.AddrDTO;

/**
 * @author beekhon.ong
 */
@FeignClient(contextId = "addr-service", name = "common", path="/${api.version.common}")
public interface AddrClient {

    @GetMapping(value = "/addr/{id}")
    public AddrDTO getAddress(@PathVariable("id") Long addressId);

    @PostMapping(value = "/addr/createOrUpdate")
    public AddrDTO createOrUpdateAddress(@RequestBody @Valid UpdateAddrDTO updateAddrDTO);
    
    @GetMapping(value = "/addr/findByFilter")
	public List<AddrDTO> findByFilter(@SpringQueryMap AddressFilter addrFilter);

    @GetMapping(path = "/country/getCountryByCountryShortName/{countryShortName}")
    public CountryDTO getCountryByCountryShortName(@PathVariable("countryShortName") String countryShortName);

    @GetMapping("/state/getStatesByCountryShortName/{countryShortName}")
    public List<StateDTO> getStatesByCountryShortName(@PathVariable("countryShortName") String countryShortName);

    @GetMapping("/city/getCitiesByStateId/{stateId}")
    public List<CityDTO> getCitiesByStateId(@PathVariable("stateId") Long stateId);

    @PostMapping(value = "/addr/findByFilter")
    public List<AddrDTO> postFindByFilter(@RequestBody AddressFilter addrFilter);
}

