package com.y3technologies.masters.controller;

import com.y3technologies.masters.dto.CommonTagAndTagType;
import com.y3technologies.masters.dto.CommonTagDto;
import com.y3technologies.masters.dto.filter.CommonTagFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.service.CommonTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Common Tag Controller
 *
 * @author suxia
 *
 */

@RestController
@RequestMapping("/${api.version.masters}/commonTag")
@SuppressWarnings("rawtypes")
public class CommonTagController extends BaseController {

    @Autowired
    private CommonTagService commonTagService;

	@PostMapping(value = "/listByParam")
	public @ResponseBody ResponseEntity listByParam(@RequestBody CommonTag tag) {
		List<CommonTag> list = commonTagService.listByParam(tag);
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@GetMapping("/findByFilter")
	public ResponseEntity<List<CommonTag>> findByFilter(CommonTagFilter filter) {
		List<CommonTag> lookupList = commonTagService.findByFilter(filter);
		return ResponseEntity.status(HttpStatus.OK).body(lookupList);
	}

	@PostMapping("/findByTagAndTagType")
	public ResponseEntity<List<CommonTag>> findByTagAndTagType(@RequestBody List<CommonTagAndTagType> commonTagAndTagTypeList) {
		List<CommonTag> lookupList = commonTagService.findByTagAndTagType(commonTagAndTagTypeList);
		return ResponseEntity.status(HttpStatus.OK).body(lookupList);
	}

	@PostMapping("/create")
	public ResponseEntity create(@RequestBody @Valid CommonTagDto commonTagDto) {
		CommonTag commonTag = new CommonTag();

		try {
			// Check for duplicates
			int existedTagCount = commonTagService.countByTagAndTypeAndTenantId(commonTagDto.getTag(), commonTagDto.getTagType(), commonTagDto.getTenantId());

			if (existedTagCount > 0) {
				throw new TransactionException("exception.common.tag.location.tag.duplicate");
			}

			BeanCopier copier = BeanCopier.create(CommonTagDto.class, CommonTag.class, false);
			copier.copy(commonTagDto, commonTag, null);

			commonTagService.save(commonTag);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.OK).body(commonTag);
	}

	@PostMapping("/createOrUpdateMultiple")
	public ResponseEntity createOrUpdateMultiple(@RequestBody List<CommonTagDto> commonTagDtoList) {
		return ResponseEntity.status(HttpStatus.OK).body(commonTagService.saveMultiple(commonTagDtoList));
	}

}
