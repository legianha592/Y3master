package com.y3technologies.masters.repository;

import com.y3technologies.masters.model.UomSetting;

import java.util.List;

public interface UomSettingRepositoryCustom {

    int countByCondition(String uom, String uomGroup);
}
