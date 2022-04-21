package com.y3technologies.masters.dto;

import java.time.LocalTime;

import lombok.Data;

/**
 * @author Sivasankari Subramaniam
 */
@Data
public class MilkrunSchedulerDto extends BaseDto{

    private Long milkrunVehicleId;

    private int tripSequence;

    private int vistSequence;

    private LocalTime startTime;

    private LocalTime endTime;

    private String dayOfWeek;

    private String driverName;

    private Long driverId;

    private Long tenantId;

    private String locationShortName;

    private Long locMasterId;

    private String visitType;

    private Long id;

    private Long addressId;

    private Long contactId;
    
    private Long version;

}
