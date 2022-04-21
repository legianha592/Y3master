package com.y3technologies.masters.client;

import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.filter.AddrContactFilter;
import com.y3technologies.masters.model.comm.AddrContact;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * @author beekhon.ong
 */
@FeignClient(contextId = "addrContact-service", name = "common", path = "/${api.version.common}")
public interface AddrContactClient {

	@GetMapping(value = "/addrContact/{id}")
	public AddrContact getAddressContact(@PathVariable("id") Long addressId);

	@PostMapping(value = "/addrContact/createOrUpdate")
	public AddrContact createOrUpdateAddressContact(@RequestBody @Valid AddrContactDTO addrContactDTO);

	@PostMapping(path = "/addrContact/getAddressContactListByIds")
	public List<AddrContact> getAddressContactList(@RequestBody List<Long> ids);

	@DeleteMapping(value = "/addrContact/remove/{id}")
	public ResponseEntity deleteAddrContact(@PathVariable("id") Long addressId);

	@GetMapping("/addrContact/findByFilter")
	public List<AddrContact> findByFilter(@SpringQueryMap AddrContactFilter filter);

	@PostMapping("/addrContact/findByFilter")
	public List<AddrContact> postFindByFilter(@RequestBody AddrContactFilter filter);

	@PostMapping(path = "/country/findByCountryIsdCode")
	List<CountryDTO> findByCountryIsdCode (@RequestBody String countryIsdCode);

	@PostMapping(path = "/country/getCountryShortNameByCountryCode")
	List<CountryDTO> findBySetCountryIsdCode (@RequestBody Set<String> countryCodeSet);

	@PostMapping(path = "/country/getCountryByFullName")
	List<CountryDTO> findBySetCountryFullName (@RequestBody Set<String> countryFullNameSet);
}
