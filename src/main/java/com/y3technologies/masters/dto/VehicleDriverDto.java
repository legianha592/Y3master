package com.y3technologies.masters.dto;

import com.y3technologies.masters.model.Vehicle;
import lombok.Data;
import java.util.List;

@Data
public class VehicleDriverDto extends BaseDto {

    private List<Vehicle> vehicleList;

    private List<DriverDto> driverList;



}
