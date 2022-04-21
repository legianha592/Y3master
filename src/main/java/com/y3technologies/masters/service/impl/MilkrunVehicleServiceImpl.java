package com.y3technologies.masters.service.impl;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.y3technologies.masters.service.MilkrunVehicleService;
import com.y3technologies.masters.service.OperationLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.y3technologies.masters.dto.MilkrunSchedulerDto;
import com.y3technologies.masters.dto.MilkrunTripDto;
import com.y3technologies.masters.dto.MilkrunVehicleDto;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.MilkrunTrip;
import com.y3technologies.masters.model.MilkrunVehicle;
import com.y3technologies.masters.model.OperationLog;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.Vehicle;
import com.y3technologies.masters.repository.DriverRepository;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.MilkrunTripRepository;
import com.y3technologies.masters.repository.MilkrunVehicleRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.repository.VehicleRepository;

@Service
public class MilkrunVehicleServiceImpl implements MilkrunVehicleService {

    @Autowired
    private MilkrunVehicleRepository milkrunVehicleRepository;

    @Autowired
    private MilkrunTripRepository milkrunTripRepository;

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PartnersRepository partnersRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
	private LocationRepository locationRepository;
    @Autowired
    private DriverRepository driverRepository;

    @Override
    @Transactional
    public MilkrunVehicleDto create(MilkrunVehicleDto milkrunVehicleDto) {
        MilkrunVehicle milkrunVehicle = parseMilkrunVehicle(milkrunVehicleDto);
        if (null == milkrunVehicle.getId()){
            milkrunVehicle = milkrunVehicleRepository.save(milkrunVehicle);
            milkrunVehicleDto.setId(milkrunVehicle.getId());
        }

        List<MilkrunTripDto> tripSequenceDtoList =  milkrunVehicleDto.getTripDtolist();
        List<MilkrunTrip> tripList = createTripSequence(tripSequenceDtoList,milkrunVehicle.getId(),milkrunVehicle.getVersion());
        List<MilkrunTripDto> tripDtoList = new ArrayList<>();
        for (MilkrunTrip trip : tripList){
            MilkrunTripDto tripDto = parseToMilkrunTripDto(trip);
            tripDtoList.add(tripDto);
        }
        milkrunVehicleDto = parseMilkrunVehicleDto(milkrunVehicle);
        milkrunVehicleDto.setTripDtolist(tripDtoList);
        operationLogService.log(true,milkrunVehicle,milkrunVehicle.getClass().getSimpleName(),milkrunVehicle.getId().toString());

        return milkrunVehicleDto;
    }

    @Override
    public boolean validateTripLocation(MilkrunVehicleDto dto) {
        List<MilkrunTripDto> tripDtoList = sortTrips(dto.getTripDtolist());
        if (1 < tripDtoList.size()){
            for (int i = 1; i < tripDtoList.size(); i++){
                if (tripDtoList.get(i).getTripSequence() == tripDtoList.get(i - 1).getTripSequence()){
                    if (tripDtoList.get(i).getLocationId().equals(tripDtoList.get(i - 1).getLocationId())){
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean changeTripActive(Long milkrunTripDtoId, boolean active) {
        MilkrunTrip milkrunTrip = milkrunTripRepository.findById(milkrunTripDtoId).get();
        milkrunTrip.setActiveInd(active);
        milkrunTrip = milkrunTripRepository.save(milkrunTrip);
        operationLogService.log(OperationLog.Operation.UPDATE_STATUS, active, milkrunTrip.getClass().getSimpleName(), milkrunTrip.getId().toString());

        return true;
    }

    @Override
    public MilkrunVehicleDto ChangeMilkrunScheduleActive(Long id, boolean active) {
        List<MilkrunTrip> tripList = milkrunTripRepository.queryByMilkrunVehicleId(id);
        MilkrunVehicle milkrunVehicle = milkrunVehicleRepository.findById(id).get();
        milkrunVehicle.setActiveInd(active);
        milkrunVehicle = milkrunVehicleRepository.save(milkrunVehicle);
        operationLogService.log(OperationLog.Operation.UPDATE_STATUS, active, milkrunVehicle.getClass().getSimpleName(), milkrunVehicle.getId().toString());
        MilkrunVehicleDto milkrunVehicleDto = parseMilkrunVehicleDto(milkrunVehicle);
        List<MilkrunTripDto> tripDtoList = new ArrayList<>();
        for (MilkrunTrip milkrunTrip : tripList){
            milkrunTrip.setActiveInd(active);
            MilkrunTripDto milkrunTripDto = parseToMilkrunTripDto(milkrunTrip);
            tripDtoList.add(milkrunTripDto);
            operationLogService.log(OperationLog.Operation.UPDATE_STATUS, active, milkrunTrip.getClass().getSimpleName(), milkrunTrip.getId().toString());
        }
        milkrunVehicleDto.setTripDtolist(tripDtoList);
        milkrunTripRepository.saveAll(tripList);

        return milkrunVehicleDto;
    }

    @Override
    public MilkrunVehicleDto queryMilkrunScheduleById(Long id) {
//        List<MilkrunTrip> tripList = milkrunTripRepository.queryByMilkrunVehicleId(id);
        MilkrunVehicle milkrunVehicle = milkrunVehicleRepository.findById(id).get();
//        List<MilkrunTripDto> tripDtoList = new ArrayList<>();
//        for (MilkrunTrip trip : tripList){
//            MilkrunTripDto milkrunTripDto = parseToMilkrunTripDto(trip);
//            tripDtoList.add(milkrunTripDto);
//        }
        MilkrunVehicleDto milkrunVehicleDto = parseMilkrunVehicleDto(milkrunVehicle);
//        if (milkrunVehicleDto.getActiveInd()){
//            milkrunVehicleDto.setTripDtolist(tripDtoList);
//        }
        return milkrunVehicleDto;
    }

    @Override
    public List<MilkrunVehicleDto> queryMilkrunSchedule(Long tenantId) {
        List<MilkrunVehicle> milkrunVehicleList = milkrunVehicleRepository.findByTenantIdAndActiveInd(tenantId, true);
        List<MilkrunVehicleDto> milkrunVehicleDtoList = new ArrayList<>();
        for (MilkrunVehicle milkrunVehicle : milkrunVehicleList){
//            List<MilkrunTrip> tripList = milkrunTripRepository.queryByMilkrunVehicleId(milkrunVehicle.getId());
//            List<MilkrunTripDto> tripDtoList = new ArrayList<>();
//            for (MilkrunTrip trip : tripList){
//                MilkrunTripDto milkrunTripDto = parseToMilkrunTripDto(trip);
//                tripDtoList.add(milkrunTripDto);
//            }
            MilkrunVehicleDto milkrunVehicleDto = parseMilkrunVehicleDto(milkrunVehicle);
//            milkrunVehicleDto.setTripDtolist(tripDtoList);
            milkrunVehicleDtoList.add(milkrunVehicleDto);
        }
        return milkrunVehicleDtoList;
    }

    @Override
    @Transactional
    public MilkrunVehicleDto updateMilkrunVehicle(MilkrunVehicleDto dto) {
        MilkrunVehicle exist = milkrunVehicleRepository.findById(dto.getId()).get();
        MilkrunVehicle milkrunVehicle = new MilkrunVehicle();
        final BeanCopier en_to_en = BeanCopier.create(MilkrunVehicle.class, MilkrunVehicle.class, false);
        en_to_en.copy(exist,milkrunVehicle,null);
        milkrunVehicle.getMilkrunTrips().clear();
//        milkrunVehicle.setMilkrunTrips(null);
        if (null != dto.getVehicleId()){
            milkrunVehicle.setVehicle(vehicleRepository.findById(dto.getVehicleId()).orElse(null));
        }else {
        	milkrunVehicle.setVehicle(null);
        }
        if (null != dto.getDriverId()){
            milkrunVehicle.setDriver(driverRepository.findById(dto.getDriverId()).orElse(null));
        }else {
        	milkrunVehicle.setDriver(null);
        }
        milkrunVehicle.setVersion(dto.getVersion());
        milkrunVehicle = milkrunVehicleRepository.saveAndFlush(milkrunVehicle);
        dto = parseMilkrunVehicleDto(milkrunVehicle);
        operationLogService.log(false,milkrunVehicle,milkrunVehicle.getClass().getSimpleName(),milkrunVehicle.getId().toString());

        return dto;
    }

    @Transactional
    @Override
    public List<MilkrunSchedulerDto> queryMilkrunScheduleUsingTenantIdAndCustomerId(Long tenantId, Long customerId) {

        String dayOfWeek = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        List<MilkrunTrip> milkrunTripList = milkrunTripRepository.findByTenantIdAndCustomerIdAndDayOfWeek(tenantId, customerId, StringUtils.upperCase(dayOfWeek));
        List<MilkrunSchedulerDto> milkrunSchedulerDtoList = milkrunTripList.stream().map(milkrunTrip-> {
            return this.mapMilkrunSchedulerDto(milkrunTrip, tenantId, dayOfWeek);
        }).collect(Collectors.toList());
        return milkrunSchedulerDtoList;
    }

    private MilkrunSchedulerDto mapMilkrunSchedulerDto(MilkrunTrip milkrunTrip, Long tenantId, String dayOfWeek) {
        MilkrunSchedulerDto milkrunSchedulerDto = new MilkrunSchedulerDto();
        milkrunSchedulerDto.setDayOfWeek(dayOfWeek);
        milkrunSchedulerDto.setDriverId(milkrunTrip.getMilkrunVehicle().getDriver().getId());
        milkrunSchedulerDto.setDriverName(milkrunTrip.getMilkrunVehicle().getDriver().getName());
        milkrunSchedulerDto.setEndTime(milkrunTrip.getEndTime());
        milkrunSchedulerDto.setLocationShortName(milkrunTrip.getLocation().getLocName());
        milkrunSchedulerDto.setLocMasterId(milkrunTrip.getLocation().getId());
        milkrunSchedulerDto.setMilkrunVehicleId(milkrunTrip.getMilkrunVehicle().getId());
        milkrunSchedulerDto.setStartTime(milkrunTrip.getStartTime());
        milkrunSchedulerDto.setTenantId(tenantId);
        milkrunSchedulerDto.setTripSequence(milkrunTrip.getTripSequence());
        milkrunSchedulerDto.setVisitType(milkrunTrip.getTPTRequestActivity());
        milkrunSchedulerDto.setVistSequence(milkrunTrip.getVisitSequence());
        milkrunSchedulerDto.setAddressId(milkrunTrip.getLocation().getAddressId());
        milkrunSchedulerDto.setContactId(milkrunTrip.getLocation().getAddressContactId());
        milkrunSchedulerDto.setId(milkrunTrip.getId());
        milkrunSchedulerDto.setVersion(milkrunTrip.getVersion());
        return milkrunSchedulerDto;
    }

    @Transactional
    @Override
    public void delete(Long milkrunVehicleId) {
        milkrunVehicleRepository.deleteById(milkrunVehicleId);
    }

    @Transactional
    @Override
    public Optional<MilkrunVehicle> findById(Long milkrunVehicleId) {
        return milkrunVehicleRepository.findById(milkrunVehicleId);
    }

    private List<MilkrunTripDto> sortTrips(List<MilkrunTripDto> tripDtoList) {
        Collections.sort(tripDtoList, new Comparator<MilkrunTripDto>() {
            @Override
            public int compare(MilkrunTripDto o1, MilkrunTripDto o2) {
                if (o1.getTripSequence() > o2.getTripSequence()){
                    return 1;
                }
                else if (o1.getTripSequence() < o2.getTripSequence()){
                    return -1;
                }
                else {
                    if (o1.getVisitSequence() > o2.getVisitSequence()){
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }
            }
        });

        return tripDtoList;
    }

    private List<MilkrunTrip> createTripSequence(List<MilkrunTripDto> tripDtoList, long vehicleTripId,Long version){
        List<MilkrunTrip> tripList = new ArrayList<>();
        for (MilkrunTripDto milkrunTripDto : tripDtoList){
            milkrunTripDto.setMilkrunVehicleId(vehicleTripId);
            MilkrunTrip milkrunTrip = parseToMilkrunTrip(milkrunTripDto,version);
            tripList.add(milkrunTrip);
            operationLogService.log(milkrunTrip.getId() == null,milkrunTrip,milkrunTrip.getClass().getSimpleName(),milkrunTrip.getId() == null ? "" : milkrunTrip.getId().toString());

        }
        tripList = milkrunTripRepository.saveAll(tripList);
        milkrunTripRepository.flush();
        return tripList;
    }

    private MilkrunTripDto parseToMilkrunTripDto(MilkrunTrip milkrunTrip){
        final BeanCopier trip_toDto = BeanCopier.create(MilkrunTrip.class, MilkrunTripDto.class, false);
        MilkrunTripDto milkrunTripDto = new MilkrunTripDto();
        trip_toDto.copy(milkrunTrip,milkrunTripDto,null);
        if (null != milkrunTrip.getMilkrunVehicle()){
            milkrunTripDto.setMilkrunVehicleId(milkrunTrip.getMilkrunVehicle().getId());
        }
        if (null != milkrunTrip.getLocation()){
            milkrunTripDto.setLocationId(milkrunTrip.getLocation().getId());
        }

        return milkrunTripDto;
    }

    private MilkrunTrip parseToMilkrunTrip(MilkrunTripDto milkrunTripDto,Long version){
        final BeanCopier trip_toEntity = BeanCopier.create(MilkrunTripDto.class, MilkrunTrip.class, false);
        MilkrunTrip milkrunTrip = new MilkrunTrip();
        trip_toEntity.copy(milkrunTripDto,milkrunTrip,null);
        MilkrunVehicle milkrunVehicle = new MilkrunVehicle();
        milkrunVehicle.setId(milkrunTripDto.getMilkrunVehicleId());
        milkrunVehicle.setVersion(0L);
        milkrunTrip.setMilkrunVehicle(milkrunVehicle);
    	if(milkrunTripDto.getLocationId()!=null) {
			Optional<Location> location = locationRepository.findById(milkrunTripDto.getLocationId());
			location.ifPresent(value ->  milkrunTrip.setLocation(value));
		}else {
			milkrunTrip.setLocation(null);
		}

        return milkrunTrip;
    }

    private MilkrunVehicleDto parseMilkrunVehicleDto(MilkrunVehicle milkrunVehicle){
        final BeanCopier vehicle_toDto = BeanCopier.create(MilkrunVehicle.class, MilkrunVehicleDto.class, false);
        MilkrunVehicleDto milkrunVehicleDto = new MilkrunVehicleDto();
        vehicle_toDto.copy(milkrunVehicle, milkrunVehicleDto, null);
        if (null != milkrunVehicle.getCustomer()){
            milkrunVehicleDto.setCustomerId(milkrunVehicle.getCustomer().getId());
        }
        if (null != milkrunVehicle.getDriver()){
            milkrunVehicleDto.setDriverId(milkrunVehicle.getDriver().getId());
        }
        if (null != milkrunVehicle.getVehicle()){
            milkrunVehicleDto.setVehicleId(milkrunVehicle.getVehicle().getId());
        }

        Set<MilkrunTrip> milkrunTrips = milkrunVehicle.getMilkrunTrips();
        if (null != milkrunTrips){
            List<MilkrunTripDto> MilkrunTripDtoList = new ArrayList<>();
            for (MilkrunTrip milkrunTrip : milkrunTrips){
                MilkrunTripDtoList.add(parseToMilkrunTripDto(milkrunTrip));
            }
            milkrunVehicleDto.setTripDtolist(MilkrunTripDtoList);
        }

        return milkrunVehicleDto;
    }

    private MilkrunVehicle parseMilkrunVehicle(MilkrunVehicleDto milkrunVehicleDto){
        final BeanCopier vehicle_toEntity = BeanCopier.create(MilkrunVehicleDto.class, MilkrunVehicle.class, false);
        MilkrunVehicle milkrunVehicle = new MilkrunVehicle();
        vehicle_toEntity.copy(milkrunVehicleDto,milkrunVehicle,null);
        
        if(milkrunVehicleDto.getCustomerId()!=null) {
        	Optional<Partners> partners = partnersRepository.findById(milkrunVehicleDto.getCustomerId());
        	partners.ifPresent(value ->  milkrunVehicle.setCustomer(value));
		}
		if(milkrunVehicleDto.getVehicleId()!=null) {
			Optional<Vehicle> vehicle = vehicleRepository.findById(milkrunVehicleDto.getVehicleId());
			vehicle.ifPresent(value ->  milkrunVehicle.setVehicle(value));
		}
		if(milkrunVehicleDto.getDriverId()!=null) {
			Optional<Driver> driver = driverRepository.findById(milkrunVehicleDto.getDriverId());
			driver.ifPresent(value -> milkrunVehicle.setDriver(value));
		}
        
		/*
		 * List<MilkrunTripDto> tripDtolist = milkrunVehicleDto.getTripDtolist();
		 * Set<MilkrunTrip> milkrunTrips = new HashSet<>(); for (MilkrunTripDto
		 * milkrunTripDto : tripDtolist){
		 * milkrunTrips.add(parseToMilkrunTrip(milkrunTripDto)); }
		 * milkrunVehicle.setMilkrunTrips(milkrunTrips);
		 */

        return milkrunVehicle;
    }
}
