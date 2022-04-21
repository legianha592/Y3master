package com.y3technologies.masters.repository;

import java.util.List;

public interface VehicleRepositoryCustom {
    List<Long> findVehicleIdsByVehiclePlateNo(Long tenantId, String vehiclePlateNo);
}
