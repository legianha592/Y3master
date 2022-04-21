package com.y3technologies.masters.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.y3technologies.masters.model.QVehicle;
import com.y3technologies.masters.model.Vehicle;
import com.y3technologies.masters.repository.VehicleRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.List;

public class VehicleRepositoryImpl implements VehicleRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    @Override
    public List<Long> findVehicleIdsByVehiclePlateNo(Long tenantId, String vehiclePlateNo) {
        QVehicle vehicle = QVehicle.vehicle;
        BooleanExpression predicate = vehicle.isNotNull();
        predicate = predicate.and(vehicle.tenantId.eq(tenantId));
        predicate = predicate.and(vehicle.vehicleRegNumber.containsIgnoreCase(vehiclePlateNo));
        JPQLQuery<Vehicle> query = new JPAQuery<>(entityManager);
        return query.from(vehicle).where(predicate).select(vehicle.id).fetch();
    }
}
