package com.y3technologies.masters.client;

import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.model.comm.Country;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(contextId = "country-service", name = "common", path = "/${api.version.common}")
public interface CountryClient {
    @GetMapping("country/getAllActiveCountries")
    public List<CountryDTO> getAllActiveCountries();

    @PostMapping("/country/getCountryByShortName")
    Country getCountryByShortName(String countryShortName);
}

