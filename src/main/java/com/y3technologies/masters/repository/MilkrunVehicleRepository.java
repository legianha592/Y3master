package com.y3technologies.masters.repository;

import java.util.List;

import com.y3technologies.masters.model.MilkrunVehicle;

public interface MilkrunVehicleRepository extends BaseRepository<MilkrunVehicle> {

    List<MilkrunVehicle> findByActiveInd(boolean activeInd);

    List<MilkrunVehicle> findByTenantIdAndActiveInd(Long tenantId, boolean activeInd);
}
