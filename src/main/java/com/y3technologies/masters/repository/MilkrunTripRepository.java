package com.y3technologies.masters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.MilkrunTrip;

@Repository
public interface MilkrunTripRepository extends BaseRepository<MilkrunTrip> {

    void deleteByMilkrunVehicleId(long milkrunVehicleId);

    List<MilkrunTrip> queryByMilkrunVehicleId(long milkrunVehicleId);

    @Query("SELECT mt FROM MilkrunTrip mt WHERE mt.milkrunVehicle.tenantId = ?1 and mt.milkrunVehicle.customer.id = ?2 and UPPER(mt.dayOfWeek) LIKE %?3% and mt.activeInd = 1 and mt.milkrunVehicle.activeInd = 1")
    List<MilkrunTrip> findByTenantIdAndCustomerIdAndDayOfWeek(Long tenantId, Long customerId, String dayOfWeek);
}
