package com.y3technologies.masters.dto.filter;

import com.y3technologies.masters.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;

/**
 * {@code CommonTag}
 *
 * @author Su Xia
 * @since 2019/11/18
 */
@Getter
@Setter
@NoArgsConstructor
public class CommonTagFilter extends ListingFilter {

    private static final long serialVersionUID = 1L;

	private String tagType;
	
	private String tag;
	
	private Long tenantId;
	
	private String referenceFunction;

	private Long referenceId;

	private Boolean active = true;

    private List<Long> commonTagIdList;

    public CommonTagFilter(List<Long> commonTagIdList) {
        this.commonTagIdList = commonTagIdList;
    }
}
