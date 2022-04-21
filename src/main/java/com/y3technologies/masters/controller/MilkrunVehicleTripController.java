package com.y3technologies.masters.controller;

import com.y3technologies.masters.dto.MilkrunSchedulerDto;
import com.y3technologies.masters.dto.MilkrunTripDto;
import com.y3technologies.masters.dto.MilkrunVehicleDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.service.MilkrunVehicleService;
import com.y3technologies.masters.util.BootResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/${api.version.masters}/milkrunVehicleTrip")
public class MilkrunVehicleTripController {

    @Autowired
    private MilkrunVehicleService milkrunVehicleService;

    @PostMapping(value = "/save")
    public @ResponseBody BootResponse save(@RequestBody @Valid MilkrunVehicleDto milkrunVehicleDto){
        BootResponse response = new BootResponse();
//        if (!milkrunVehicleService.validateTripLocation(milkrunVehicleDto)){
//            response.setResult(ConstantVar.FAILURE);
//            response.setMessage("Location setting is error!");
//            response.setCode(500);
//            return response;
//        }
        try {
        	 milkrunVehicleDto = milkrunVehicleService.create(milkrunVehicleDto);
		} catch (ObjectOptimisticLockingFailureException  e) {
			throw new TransactionException("exception.milkrun.version");
		}
        catch (Exception  e) {
			throw new TransactionException("exception.milkrun.version");
		}
       
        response.setData(milkrunVehicleDto);
        response.setCode(200);

        return response;
    }

    @PostMapping(value = "updateMilkrunVehicle")
    public @ResponseBody BootResponse updateMilkrunVehicle(@RequestBody MilkrunVehicleDto milkrunVehicleDto){
    	try {
    		milkrunVehicleDto = milkrunVehicleService.updateMilkrunVehicle(milkrunVehicleDto);
		} catch (ObjectOptimisticLockingFailureException  e) {
			throw new TransactionException("exception.milkrun.version");
		}
    	
        BootResponse response = new BootResponse();
        response.setCode(200);
        response.setData(milkrunVehicleDto);

        return response;
    }

    @PostMapping(value = "changeTripActive")
    public @ResponseBody BootResponse changeTripActive(@RequestBody MilkrunTripDto milkrunTripDto){
        milkrunVehicleService.changeTripActive(milkrunTripDto.getId(),milkrunTripDto.getActiveInd());
        BootResponse response = new BootResponse();
        response.setCode(200);

        return response;
    }

    @PostMapping(value = "ChangeMilkrunScheduleActive")
    public @ResponseBody BootResponse ChangeMilkrunScheduleActive(@RequestBody MilkrunVehicleDto milkrunVehicleDto){
        MilkrunVehicleDto result = milkrunVehicleService.ChangeMilkrunScheduleActive(milkrunVehicleDto.getId(),milkrunVehicleDto.getActiveInd());
        BootResponse response = new BootResponse();
        response.setCode(200);
        if (milkrunVehicleDto.getActiveInd()){
            response.setData(result);
        }

        return response;
    }

    @GetMapping(value = "queryMilkrunSchedule")
    public @ResponseBody BootResponse queryMilkrunSchedule(@RequestParam(name = "id",required = false) Long milkrunVehicleId, @RequestParam(name = "tenantId",required = false) Long tenantId){
        BootResponse response = new BootResponse();
        response.setCode(200);
        if (null == milkrunVehicleId){
            List<MilkrunVehicleDto> list = milkrunVehicleService.queryMilkrunSchedule(tenantId);
            response.setData(list);
        }
        else {
            MilkrunVehicleDto dto = milkrunVehicleService.queryMilkrunScheduleById(milkrunVehicleId);
            response.setData(dto);
        }
        return response;
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful return"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @GetMapping("/queryMilkrunScheduleUsingTenantAndCustomer")
    @Operation(description = "Query Milkrun Trips based on Tenant and Customer ID")
    public ResponseEntity<Map<Long, List<MilkrunSchedulerDto>>> queryMilkrunScheduleUsingTenantAndCustomer(Long tenantId, Long customerId){
        List<MilkrunSchedulerDto> milkrunSchedulerDtoList = milkrunVehicleService.queryMilkrunScheduleUsingTenantIdAndCustomerId(tenantId, customerId);
        Map<Long, List<MilkrunSchedulerDto>> schedulerDtoMap = milkrunSchedulerDtoList.stream().collect(Collectors.groupingBy(MilkrunSchedulerDto::getMilkrunVehicleId));
        return ResponseEntity.status(HttpStatus.OK).body(schedulerDtoMap);
    }

}
