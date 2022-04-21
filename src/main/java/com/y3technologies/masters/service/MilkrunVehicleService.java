package com.y3technologies.masters.service;

import java.util.List;
import java.util.Optional;

import com.y3technologies.masters.dto.MilkrunSchedulerDto;
import com.y3technologies.masters.dto.MilkrunVehicleDto;
import com.y3technologies.masters.model.MilkrunVehicle;

public interface MilkrunVehicleService {

    MilkrunVehicleDto create(MilkrunVehicleDto dto);

    boolean validateTripLocation(MilkrunVehicleDto dto);

    boolean changeTripActive(Long milkrunTripDtoId, boolean active);

    MilkrunVehicleDto ChangeMilkrunScheduleActive(Long id, boolean active);

    MilkrunVehicleDto queryMilkrunScheduleById(Long id);

    List<MilkrunVehicleDto> queryMilkrunSchedule(Long tenantId);

    MilkrunVehicleDto updateMilkrunVehicle(MilkrunVehicleDto dto);

    List<MilkrunSchedulerDto> queryMilkrunScheduleUsingTenantIdAndCustomerId(Long tenantId, Long customerId);

    void delete(Long milkrunVehicleId);

    Optional<MilkrunVehicle> findById(Long milkrunVehicleId);
}
