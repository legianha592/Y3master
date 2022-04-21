package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.MilestoneConfiguredStatusDTO;
import com.y3technologies.masters.dto.MilestoneDTO;
import com.y3technologies.masters.dto.MilestoneExcelDto;
import com.y3technologies.masters.dto.MilestoneStatusConfigDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.dto.filter.MilestoneFilter;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Milestone;
import com.y3technologies.masters.model.MilestoneStatusConfig;
import com.y3technologies.masters.model.OperationLog.Operation;
import com.y3technologies.masters.model.QMilestone;
import com.y3technologies.masters.repository.MilestoneRepository;
import com.y3technologies.masters.service.CommonTagService;
import com.y3technologies.masters.service.FtpJSchService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.MilestoneService;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.util.EntitySpecificationBuilder;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import com.y3technologies.masters.util.SortingUtils;
import com.y3technologies.masters.util.ValidationUtils;
import com.y3technologies.masters.util.DateFormatUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    private static final Logger logger = LoggerFactory.getLogger(MilestoneServiceImpl.class);
    private final MilestoneRepository milestoneRepository;
    private final OperationLogService operationLogService;
    private final ModelMapper modelMapper;
    private final LookupService lookupService;
    private final String FILE_NAME = "default_milestones";
    private final String FILE_EXTENSION = "xlsx";
    private final String MILESTONE_UPDATE_STATUS_TYPE = "MilestoneUpdateStatusType";
    private final String TPT_REQUEST_STATUS_TYPE = "TptRequestStatusType";

    @Autowired
    private CommonTagService commonTagService;

    @Autowired
    private ExcelClient excelClient;

    @Autowired
    private ExcelUtils excelUtils;

    @Autowired
    private MastersApplicationPropertiesConfig mastersApplicationPropertiesConfig;

    @Autowired
    private MessagesUtilities messagesUtilities;

    @Autowired
    private MastersApplicationPropertiesConfig propertiesConfig;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private FtpJSchService ftpJSchService;

    @Autowired
    private AasClient aasClient;

    List<String> milestoneExcludeGroup = Arrays.asList(
            AppConstants.Milestone.ORDER_RECEIVED,
            AppConstants.Milestone.PLANNED_WITH_TRANSPORTER,
            AppConstants.Milestone.PLANNED_WITH_VEHICLE_DRIVER,
            AppConstants.Milestone.UNASSIGN_TRANSPORTER,
            AppConstants.Milestone.UNASSIGN_VEHICLE_DRIVER,
            AppConstants.Milestone.UPDATED_TRANSPORTER,
            AppConstants.Milestone.UPDATED_VEHICLE_DRIVER);

    public MilestoneServiceImpl(MilestoneRepository milestoneRepository, OperationLogService operationLogService,
                                ModelMapper modelMapper, LookupService lookupService) {
        this.milestoneRepository = milestoneRepository;
        this.operationLogService = operationLogService;
        this.modelMapper = modelMapper;
        this.lookupService = lookupService;
    }

    @Override
    @Transactional
    public Milestone save(Milestone model) {
        Long id = model.getId();
        Boolean updateFlag = Boolean.FALSE;
        if (model.getId() != null) {
            updateFlag = Boolean.TRUE;
        }
        milestoneRepository.save(model);

        if (updateFlag) {
            operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
        }

        return model;
    }

    @Override
    public boolean existsById(Long id) {
        return milestoneRepository.existsById(id);
    }

    @Override
    public List<Milestone> listByParam(Milestone milestone) {
        EntitySpecificationBuilder<Milestone> es = new EntitySpecificationBuilder<Milestone>();
        es.with("tenantId", ":", milestone.getTenantId(), "and");
        List<Milestone> list = milestoneRepository.findAll(es.build());
        return list;
    }

    @Override
    public DataTablesOutput<Milestone> query(DataTablesInput input) {
        if (input.getColumn("tenantId") != null
                && StringUtils.isNotEmpty(input.getColumn("tenantId").getSearch().getValue())
                && input.getColumn("tenantId").getSearchable()) {
            Specification<Milestone> sTenantId = new Specification<Milestone>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Predicate toPredicate(Root<Milestone> root, CriteriaQuery<?> query,
                                             CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("tenantId"),
                            input.getColumn("tenantId").getSearch().getValue());
                }
            };
            input.getColumn("tenantId").setSearchable(false);

            return milestoneRepository.findAll(input, sTenantId);
        }
        return milestoneRepository.findAll(input);
    }

    @Override
    public Milestone getById(Long id) {
        return milestoneRepository.getById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Boolean status) {
        Milestone model = getById(id);
        if (model.getActiveInd() != status) {
            milestoneRepository.updateStatus(id, status);
            operationLogService.log(Operation.UPDATE_STATUS, status, model.getClass().getSimpleName(), id.toString());
        }
    }

    @Override
    public List<Milestone> findByFilter(MilestoneFilter filter) {

        QMilestone milestone = QMilestone.milestone;
        BooleanExpression query = milestone.isNotNull();
        OrderSpecifier<Integer> orderSpecifier = milestone.sequence.asc().nullsLast();

        if (filter.getTenantId() != null) {
            query = query.and(milestone.tenantId.eq(filter.getTenantId()));
        }

        if (StringUtils.isNoneBlank(filter.getMilestoneCode())) {
            String keyword = filter.getMilestoneCode().trim();
            query = query.and(milestone.milestoneCode.eq(keyword));
        }

        if (filter.getIsMilestoneUpdate()) {
            query = query.and(milestone.milestoneCode.notIn(milestoneExcludeGroup));
        }

        if (filter.getActive() != null) {
            query = query.and(milestone.activeInd.eq(filter.getActive()));
        }

        List<Milestone> mappedResults = new ArrayList<Milestone>();
        Iterable<Milestone> results = milestoneRepository.findAll(query, orderSpecifier);
        results.forEach(mappedResults::add);
        return mappedResults;
    }

    @Override
    public Milestone createOrUpdate(MilestoneDTO milestoneDTO) {

        Milestone milestone = new Milestone();

        if (!ValidationUtils.Number.isValidId(milestoneDTO.getId())) {
            milestone = modelMapper.map(milestoneDTO, Milestone.class);
        } else {
            milestone = this.getById(milestoneDTO.getId());
            modelMapper.map(milestoneDTO, milestone);
        }

        Set<MilestoneStatusConfig> statusConfigList = new HashSet<>();
        Set<MilestoneStatusConfig> existingStatusConfigList = milestone.getMilestoneStatusConfigList();


        for (MilestoneStatusConfigDTO config : milestoneDTO.getMilestoneStatusConfigDtoList()) {
            String i = lookupService.findById(config.getLookupId()).getLookupCode();
            MilestoneStatusConfig statusConfig = modelMapper.map(config, MilestoneStatusConfig.class);
            Lookup tptRequestStatusLookup = getTptRequestStatusLookup(i, statusConfig, milestone);
            statusConfig.setTptRequestStatusTypeId(null != tptRequestStatusLookup ? tptRequestStatusLookup.getId() : null);
            if (null == statusConfig.getId()) {
                milestone.addMilestoneStatusConfig(statusConfig);
            } else {
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(existingStatusConfigList)) {
                    Optional<MilestoneStatusConfig> milestoneStatusConfigOptional = existingStatusConfigList.stream().filter(j -> j.getId().equals(config.getId())).findFirst();
                    if (milestoneStatusConfigOptional.isPresent()) {
                        MilestoneStatusConfig existingMilestoneConfig = milestoneStatusConfigOptional.get();
                        existingMilestoneConfig.setActivated(statusConfig.getActivated());
                        statusConfigList.add(existingMilestoneConfig);
                    }
                }
            }
        }
        return this.save(milestone);
    }

    private Lookup getTptRequestStatusLookup(String i, MilestoneStatusConfig statusConfig, Milestone milestone) {
        if (AppConstants.Milestone.DROP_OFF.equalsIgnoreCase(milestone.getMilestoneCode())) {
            switch (i.trim()) {
                case AppConstants.MilestoneStatus.COMPLETE:
                    return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.COMPLETE);
                case AppConstants.MilestoneStatus.PARTIAL:
                    return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.PARTIAL);
                case AppConstants.MilestoneStatus.FAILED:
                    return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.FAILED);
            }

        } else if (AppConstants.Milestone.PICK_UP.equalsIgnoreCase(milestone.getMilestoneCode())) {
            switch (i.trim()) {
                case AppConstants.MilestoneStatus.COMPLETE:
                    return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.PICK_UP);
                case AppConstants.MilestoneStatus.FAILED:
                    return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.FAILED);
            }
        } else {
            if (i.equalsIgnoreCase("ORDER RECEIVED") || i.equalsIgnoreCase("UNASSIGN TRANSPORTER")) {
                return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.NEW);
            } else if (i.equalsIgnoreCase("PLANNED WITH TRANSPORTER") || i.equalsIgnoreCase("PLANNED WITH VEHICLE DRIVER")
                    || i.equalsIgnoreCase("UNASSIGN VEHICLE DRIVER") || i.equalsIgnoreCase("UPDATED TRANSPORTER")
                    || i.equalsIgnoreCase("UPDATED VEHICLE DRIVER")) {
                return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.PLAN);
            } else if (i.equalsIgnoreCase("REQUEST DISPATCHED") || i.equalsIgnoreCase("ARRIVE FOR PICK UP")) {
                return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.DISPATCH);
            } else if (i.equalsIgnoreCase("ARRIVE FOR DROP OFF")) {
                return getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.PICK_UP);
            }
        }
        return null;
    }

    @Override
    public MilestoneDTO findMilestoneDTOById(Long id) {

        if (!ValidationUtils.Number.isValidId(id))
            throw new TransactionException("invalid.milestone.id");

        Milestone milestone = this.getById(id);

        if (ObjectUtils.isEmpty(milestone))
            throw new TransactionException("invalid.milestone.id");

        MilestoneDTO milestoneDTO = modelMapper.map(milestone, MilestoneDTO.class);
        milestoneDTO.setMilestoneCodeDescription(
                messagesUtilities.getResourceMessage(milestone.getMilestoneCode(), LocaleContextHolder.getLocale()));

        List<MilestoneStatusConfigDTO> statusConfigDTOList = new ArrayList<>();

        milestone.getMilestoneStatusConfigList().forEach(config -> {
            MilestoneStatusConfigDTO configDTO = modelMapper.map(config, MilestoneStatusConfigDTO.class);

            statusConfigDTOList.add(configDTO);
        });

        milestoneDTO.setMilestoneStatusConfigDtoList(statusConfigDTOList);

        return milestoneDTO;
    }

    @Override
    public String findByMilestoneCodeAndMilestoneStatusAndTenantId(String milestoneCode, String milestoneStatus, Long tenantId) {
        String tptRequestStatusLookupString = null;
        MilestoneFilter milestoneFilter = new MilestoneFilter();
        milestoneFilter.setTenantId(tenantId);
        milestoneFilter.setMilestoneCode(milestoneCode);
        List<Milestone> milestoneList = this.findByFilter(milestoneFilter);
        Milestone milestone = null;

        // milestone unique by tenantId and milestoneCode
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(milestoneList)) {
            milestone = milestoneList.get(0);
        }

        Optional<MilestoneStatusConfig> milestoneStatusConfigOptional = milestone.getMilestoneStatusConfigList().stream().filter(i -> milestoneStatus.equalsIgnoreCase(lookupService.findById(i.getLookupId()).getLookupCode())).findFirst();
        List<Long> tptRequestStatusTypeIdList = milestone.getMilestoneStatusConfigList().stream().map(MilestoneStatusConfig::getTptRequestStatusTypeId).collect(Collectors.toList());

        if (milestoneStatusConfigOptional.isPresent()) {
            Long tptRequestStatusTypeId = milestoneStatusConfigOptional.get().getTptRequestStatusTypeId();
            if (null != tptRequestStatusTypeId) {
                Lookup tptRequestStatusLookup = lookupService.findById(tptRequestStatusTypeId);
                tptRequestStatusLookupString = tptRequestStatusLookup.getLookupCode();
            }
        }
        return tptRequestStatusLookupString;
    }

    @Override
    public int countByCondition(String milestoneCode, Long tenantId){
        return milestoneRepository.countByMilestoneCodeAndTenantId(milestoneCode, tenantId);
    }

    @Override
    public List<Milestone> findByTenantId(Long tenantId, MilestoneFilter filter) {
        QMilestone qMilestone = QMilestone.milestone;
        BooleanExpression query = qMilestone.isNotNull();
        query = query.and(qMilestone.tenantId.eq(tenantId));

        if (!StringUtils.isBlank(filter.getMilestoneCode())) {
            query = query.and(qMilestone.milestoneCode.like("%" + filter.getMilestoneCode() + "%"));
        }

        if (!StringUtils.isBlank(filter.getMilestoneDescription())) {
            query = query.and(qMilestone.milestoneDescription.like("%" + filter.getMilestoneDescription() + "%"));
        }

        if (!StringUtils.isBlank(filter.getCustomerDescription())) {
            query = query.and(qMilestone.customerDescription.like("%" + filter.getCustomerDescription() + "%"));
        }

        if (!StringUtils.isBlank(filter.getMilestoneCategory())) {
            query = query.and(qMilestone.milestoneCategory.like("%" + filter.getMilestoneCategory() + "%"));
        }

        if (!Objects.isNull(filter.getIsInternal())) {
            query = query.and(qMilestone.isInternal.eq(filter.getIsInternal()));
        }

        return milestoneRepository.findAll(query);
    }

    private void sortByMultiFields(List<Sort.Order> lstSort, List<Milestone> milestoneList) {
        Comparator<Milestone> comparator = Comparator.comparing(Milestone::getMilestoneCode, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        if (lstSort.isEmpty()) {
            milestoneList.sort(comparator);
            return;
        }
        Comparator<String> sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(0).getDirection().isAscending());
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_CODE)) {
            comparator = Comparator.comparing(Milestone::getMilestoneCode, sortOrder);
        }
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_DESCRIPTION)) {
            comparator = Comparator.comparing(Milestone::getMilestoneDescription, sortOrder);
        }
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_CATEGORY)) {
            comparator = Comparator.comparing(Milestone::getMilestoneCategory, sortOrder);
        }
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_CUSTOMIZED_DESCRIPTION)) {
            comparator = Comparator.comparing(Milestone::getCustomerDescription, sortOrder);
        }
        if (lstSort.get(0).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_EXTERNAL)) {
            comparator = Comparator.comparing(Milestone::getExternal, sortOrder.reversed());
        }
        if (Objects.equals(1, lstSort.size())) {
            comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
            milestoneList.sort(comparator);
            return;
        }
        for (int i = 1; i < lstSort.size(); i++) {
            sortOrder = SortingUtils.getStringComparatorCaseInsensitive(lstSort.get(i).getDirection().isAscending());
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_CODE)) {
                comparator = comparator.thenComparing(Milestone::getMilestoneCode, sortOrder);
            }
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_DESCRIPTION)) {
                comparator = comparator.thenComparing(Milestone::getMilestoneDescription, sortOrder);
            }
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_CATEGORY)) {
                comparator = comparator.thenComparing(Milestone::getMilestoneCategory, sortOrder);
            }
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_CUSTOMIZED_DESCRIPTION)) {
                comparator = comparator.thenComparing(Milestone::getCustomerDescription, sortOrder);
            }
            if (lstSort.get(i).getProperty().equalsIgnoreCase(AppConstants.SortPropertyName.MILESTONES_EXTERNAL)) {
                comparator = comparator.thenComparing(Milestone::getExternal, sortOrder.reversed());
            }
        }
        comparator = SortingUtils.addDefaultSortExtraBaseEntity(comparator);
        milestoneList.sort(comparator);
    }

    @Override
    public List<MilestoneConfiguredStatusDTO> getMilestoneConfiguredStatusByFilter(MilestoneFilter filter) {

        List<Milestone> milestoneList = this.findByFilter(filter);

        if (CollectionUtils.isEmpty(milestoneList))
            return Collections.emptyList();

        List<MilestoneConfiguredStatusDTO> milestoneConfiguredList = new ArrayList<>();

        for (Milestone milestone : milestoneList) {

            if (CollectionUtils.isEmpty(milestone.getMilestoneStatusConfigList()))
                continue;

            MilestoneConfiguredStatusDTO config = modelMapper.map(milestone, MilestoneConfiguredStatusDTO.class);

            Set<Long> lookupIdSet = milestone.getMilestoneStatusConfigList().stream()
                    .filter(m -> m.getActivated() == true).map(m -> m.getLookupId()).collect(Collectors.toSet());

            if (CollectionUtils.isEmpty(lookupIdSet)) {
                config.setConfiguredStatusList(Collections.emptyList());
                continue;
            }

            LookupFilter lookupFilter = new LookupFilter();
            lookupFilter.setTenantId(filter.getTenantId());
            lookupFilter.setLookupIdList(new ArrayList<>(lookupIdSet));

            List<Lookup> lookupList = lookupService.findByFilter(lookupFilter);

            config.setConfiguredStatusList(
                    lookupList.stream().map(l -> l.getLookupCode()).collect(Collectors.toList()));

            milestoneConfiguredList.add(config);
        }

        return milestoneConfiguredList;
    }

    @Override
    @Transactional
    public void populateSystemMilestoneForUser(Long tenantId) {
        List<MilestoneExcelDto> milestoneExcelDtoList = getMilestonesFromExcel();
        List<Milestone> milestoneList = new ArrayList<>();
        milestoneExcelDtoList.forEach(milestone -> {
            milestoneList.add(mapMilestoneFromExcel(milestone, tenantId));
        });
        milestoneRepository.saveAll(milestoneList);

    }

    private Milestone mapMilestoneFromExcel(MilestoneExcelDto milestoneExcelDto, Long tenantId) {
        Milestone milestone = new Milestone();
        milestone = modelMapper.map(milestoneExcelDto, Milestone.class);
        milestone.setTenantId(tenantId);
        milestone.setIsDefault(Boolean.TRUE);
        // milestone and lookup config
        List<String> milestoneStatuses = Arrays.asList(milestoneExcelDto.getMilestoneStatus().split(","));
        List<String> tptRequestStatuses = new ArrayList<>();
        if (null != milestoneExcelDto.getTptRequestStatus()) {
            tptRequestStatuses = Arrays.asList(milestoneExcelDto.getTptRequestStatus().split(","));
        }
        Milestone finalMilestone = milestone;
        List<String> finalTptRequestStatuses = tptRequestStatuses;
        milestoneStatuses.forEach(i -> {
            MilestoneStatusConfig milestoneStatusConfig = new MilestoneStatusConfig();
            milestoneStatusConfig.setMilestone(finalMilestone);
            Lookup milestoneLookup = getLookup(MILESTONE_UPDATE_STATUS_TYPE, i.trim());
            Lookup tptRequestStatusLookup = null;
            if (AppConstants.Milestone.DROP_OFF.equalsIgnoreCase(finalMilestone.getMilestoneCode())) {
                switch (i.trim()) {
                    case AppConstants.MilestoneStatus.COMPLETE:
                        tptRequestStatusLookup = getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.COMPLETE);
                        break;
                    case AppConstants.MilestoneStatus.PARTIAL:
                        tptRequestStatusLookup = getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.PARTIAL);
                        break;
                    case AppConstants.MilestoneStatus.FAILED:
                        tptRequestStatusLookup = getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.FAILED);
                        break;
                }

            } else if (AppConstants.Milestone.PICK_UP.equalsIgnoreCase(finalMilestone.getMilestoneCode())) {
                switch (i.trim()) {
                    case AppConstants.MilestoneStatus.COMPLETE:
                        tptRequestStatusLookup = getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.PICK_UP);
                        break;
                    case AppConstants.MilestoneStatus.FAILED:
                        tptRequestStatusLookup = getLookup(TPT_REQUEST_STATUS_TYPE, AppConstants.TptRequestStatus.FAILED);
                        break;
                }
            } else {
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(finalTptRequestStatuses)) {
                    tptRequestStatusLookup = getLookup(TPT_REQUEST_STATUS_TYPE, finalTptRequestStatuses.get(0));
                }
            }
            milestoneStatusConfig.setTptRequestStatusTypeId(null != tptRequestStatusLookup ? tptRequestStatusLookup.getId() : null);
            milestoneStatusConfig.setLookupId(null != milestoneLookup ? milestoneLookup.getId() : null);
            milestoneStatusConfig.setActivated(Boolean.TRUE);
            finalMilestone.addMilestoneStatusConfig(milestoneStatusConfig);
        });

        return finalMilestone;
    }

    private Lookup getLookup(String lookupType, String lookupCode) {
        LookupFilter lookupFilter = new LookupFilter();
        lookupFilter.setTenantId(0L); // system value
        lookupFilter.setLookupCode(lookupCode);
        lookupFilter.setLookupType(lookupType);
        return lookupService.findByLookupCodeLookupTypeTenantId(lookupFilter);
    }

    private List<MilestoneExcelDto> getMilestonesFromExcel() {
        List<MilestoneExcelDto> milestoneDTOList = new ArrayList<>();
        try {
            StringBuilder filePath = new StringBuilder().append(mastersApplicationPropertiesConfig.getFileServerBasePath());
            filePath.append("/").append(AppConstants.FTP_DEFAULT_FOLDER_NAME).append("/").append(FILE_NAME).append(".").append(FILE_EXTENSION);
            InputStream in = ftpJSchService.readFile(filePath.toString());
            StringBuilder fileName = new StringBuilder().append(FILE_NAME).append(".").append(FILE_EXTENSION);
            MultipartFile file = new MockMultipartFile(fileName.toString(), fileName.toString(), ContentType.APPLICATION_OCTET_STREAM.toString(), in);
            milestoneDTOList = excelUtils.readExcel(MilestoneExcelDto.class, file);
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }        return milestoneDTOList;
    }

    @Override
    public ExcelResponseMessage uploadExcel(MultipartFile file, Long currentTenantId){
        Long timeStartReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
        Map<Integer, StringBuilder> mapExcelError = new HashMap();
        Set<String> excelEmailErrors = new HashSet<>();
        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = excelUtils.getExcelTemplate(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_UPLOAD);
        byte[] byteArr = excelUtils.getByteArrOfUploadedFile(file);
        ExcelResponseMessage excelResponseMessage = new ExcelResponseMessage();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";

        if (!excelUtils.validFileType(fileName, excelResponseMessage)){
            return excelResponseMessage;
        }

        Workbook workbook = excelUtils.initWorkbook(byteArr, fileName);

        if (!excelUtils.validSheetNumber(workbook, uploadTemplateHdrIdDto.getSheetSeqNo(), excelResponseMessage)){
            return excelResponseMessage;
        }

        Sheet sheet = workbook.getSheetAt(uploadTemplateHdrIdDto.getSheetSeqNo());

        boolean validHeader = excelUtils.validateExcelHeader(MilestoneDTO.class, workbook, sheet, fileName
                , uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors, excelResponseMessage);

        if (!validHeader){
            return excelResponseMessage;
        }

        if (excelUtils.checkEmptyFile(sheet, uploadTemplateHdrIdDto.getStartRow(), excelResponseMessage)){
            return excelResponseMessage;
        }

        excelUtils.calculateTimeForPopup(sheet, excelResponseMessage);

        SessionUserInfoDTO sessionUserInfoDTO = (SessionUserInfoDTO)
                RequestContextHolder.currentRequestAttributes().getAttribute("SESSION_INFO", RequestAttributes.SCOPE_REQUEST);
        if (org.springframework.util.ObjectUtils.isEmpty(sessionUserInfoDTO)){
            throw new TransactionException("exception.tenant.id.invalid");
        }
        UpdateUserProfileDTO userInfo = aasClient.retrieveUserProfile(sessionUserInfoDTO.getAasUserId());

        executor.execute(() -> {
            try {
                validateAndSaveExcelData(currentTenantId, workbook, sheet, fileName, mapExcelError, excelEmailErrors, uploadTemplateHdrIdDto, timeStartReadingFile, userInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return excelResponseMessage;
    }

    public void checkDuplicatedEntries(List<MilestoneDTO> listMilestoneDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId) {
        Map<String, String> mapCheckDup = new HashMap<>();
        for (MilestoneDTO dto : listMilestoneDto) {
            if (StringUtils.isEmpty(dto.getMilestoneCode())) {
                continue;
            }
            String listPositionDup = mapCheckDup.get(dto.getTenantId() + "||" + dto.getMilestoneCode().trim().toLowerCase());
            if (StringUtils.isEmpty(listPositionDup)) {
                listPositionDup = StringUtils.EMPTY + dto.getExcelRowPosition();
            } else {
                listPositionDup += ", " + dto.getExcelRowPosition();
            }
            mapCheckDup.put(dto.getTenantId() + "||" + dto.getMilestoneCode().trim().toLowerCase(), listPositionDup);
        }

        for (int itemIndex = 0; itemIndex < listMilestoneDto.size(); itemIndex++) {
            MilestoneDTO dto = listMilestoneDto.get(itemIndex);
            if (StringUtils.isEmpty(dto.getMilestoneCode())) {
                continue;
            }
            String listPositionDup = mapCheckDup.get(dto.getTenantId() + "||" + dto.getMilestoneCode().trim().toLowerCase());
            if (!listPositionDup.contains(",")) {
                continue;
            }
            StringBuilder cellErrors = mapExcelError.get(itemIndex);
            if (cellErrors == null) {
                cellErrors = new StringBuilder();
            }
            excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("milestone.excel.duplicate.line", new String[]{mapCheckDup.get(dto.getTenantId() + "||" + dto.getMilestoneCode().trim().toLowerCase()), dto.getMilestoneCode()}));
            excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.entries", null));
            mapExcelError.put(itemIndex, cellErrors);
        }
    }

    private void checkValidDataEntries(List<MilestoneDTO> listMilestoneDto, Map<Integer, StringBuilder> mapExcelError, Set<String> excelEmailErrors, Long currentTenantId) {
        String[] categoryArr = getCategoryArrList(currentTenantId);
        for (int itemIndex = 0; itemIndex < listMilestoneDto.size(); itemIndex++) {
            MilestoneDTO dto = listMilestoneDto.get(itemIndex);
            if (org.springframework.util.StringUtils.isEmpty(dto.getMilestoneCode())) {
                continue;
            }
            // check valid category
            if (dto.getMilestoneCategory() != null) {
                String[] inputCategoryItems = dto.getMilestoneCategory().split("\\|");
                for (String item : inputCategoryItems) {
                    if (!Arrays.asList(categoryArr).contains(item)) {
                        StringBuilder cellErrors = mapExcelError.get(itemIndex) == null ? new StringBuilder() : mapExcelError.get(itemIndex);
                        excelUtils.buildCellErrors(cellErrors,messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Milestone Category"}));
                        excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
                        mapExcelError.put(itemIndex, cellErrors);
                    }
                }
            }
            // check valid category milestone code by tenant id
            int existedTransporterAmount = countByCondition(dto.getMilestoneCode(), dto.getTenantId());
            if (existedTransporterAmount > 0) {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }
                excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.existing.entries", new String[]{"Milestone"}));
                excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.existing.entries", null));
                mapExcelError.put(itemIndex, cellErrors);
            }
            //validate lookup code is valid
            if (dto.getEnableMilestoneStatus() != null && !dto.getEnableMilestoneStatus().isBlank()) {
                String[] lookupCodes = dto.getEnableMilestoneStatus().split("\\|");
                for (String lookupCode : lookupCodes) {
                    LookupFilter filter = new LookupFilter();
                    filter.setLookupType(AppConstants.LookupType.MILESTONE_UPDATE_STATUS_TYPE);
                    filter.setLookupCode(lookupCode);
                    filter.setTenantId(currentTenantId);
                    Lookup lookup = lookupService.findByLookupCodeLookupTypeTenantId(filter);
                    if (lookup == null) {
                        filter.setTenantId(0L);
                        lookup = lookupService.findByLookupCodeLookupTypeTenantId(filter);
                        if(lookup ==null){
                            StringBuilder cellErrors = mapExcelError.get(itemIndex) == null ? new StringBuilder() : mapExcelError.get(itemIndex);
                            excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.not.existing", new String[]{"Enable milestone status"}));
                            excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
                            mapExcelError.put(itemIndex, cellErrors);
                        }
                    }
                }
            }
        }
    }

    private String[] getCategoryArrList(Long currentTenantId)
    {
        CommonTag tag = new CommonTag();
        tag.setTagType("MilestoneCategory");
        tag.setTenantId(currentTenantId);
        List<CommonTag> categoryList = commonTagService.listByParam(tag);
        String[] ret = new String[categoryList.size()];
        int i = 0;
        for (CommonTag tagItem : categoryList) {
            ret[i++] = tagItem.getTag();
        }
        return ret;
    }

    public void saveExcelData(List<MilestoneDTO> listMilestoneDto, Map<Integer, StringBuilder> mapExcelError, Long currentTenantId, Set<String> excelEmailErrors) {
        for (int itemIndex = 0; itemIndex < listMilestoneDto.size(); itemIndex++) {
            MilestoneDTO dto = listMilestoneDto.get(itemIndex);
            dto.setMilestoneStatusConfigDtoList(getMilestoneStatusConfigDtoList(dto.getEnableMilestoneStatus(), AppConstants.UserInfo.DEFAULT_TENANT_ID));
            dto.setIsInternal(dto.getExternal());
            dto.setTenantId(currentTenantId);
            try {
                createOrUpdate(dto);
            } catch (Exception e) {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }
                excelEmailErrors.add(messagesUtilities.getMessageWithParam("upload.excel.email.saving.failed", null));
                excelUtils.buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.saving.failed", null));
                mapExcelError.put(itemIndex, cellErrors);
            }
        }
    }

    private List<MilestoneStatusConfigDTO> getMilestoneStatusConfigDtoList(String enableMilestoneStatus, Long currentTenantId) {
        List<MilestoneStatusConfigDTO> listMilestoneStatusConfig = new ArrayList<>();
        //insert default milestone status:
        LookupFilter defaultFilter = new LookupFilter();
        defaultFilter.setTenantId(currentTenantId);
        defaultFilter.setLookupType(AppConstants.LookupType.MILESTONE_UPDATE_STATUS_TYPE);
        defaultFilter.setLookupCode(AppConstants.MilestoneStatus.COMPLETE);
        Lookup defaultLookup = lookupService.findByLookupCodeLookupTypeTenantId(defaultFilter);
        if (!org.springframework.util.ObjectUtils.isEmpty(defaultLookup)){
            MilestoneStatusConfigDTO defaultMilestoneStatus = new MilestoneStatusConfigDTO();
            defaultMilestoneStatus.setLookupCode(defaultLookup.getLookupCode());
            defaultMilestoneStatus.setLookupId(defaultLookup.getId());
            defaultMilestoneStatus.setActivated(Boolean.TRUE);
            listMilestoneStatusConfig.add(defaultMilestoneStatus);
        }

        LookupFilter filter = new LookupFilter();
        filter.setTenantId(currentTenantId);
        filter.setLookupType(AppConstants.LookupType.MILESTONE_UPDATE_STATUS_TYPE);
        if (enableMilestoneStatus != null && enableMilestoneStatus.split("\\|").length > 0) {
            for (String lookupCode: enableMilestoneStatus.split("\\|")) {
                MilestoneStatusConfigDTO milestoneStatus = new MilestoneStatusConfigDTO();
                filter.setLookupCode(lookupCode);
                Lookup lookup = lookupService.findByLookupCodeLookupTypeTenantId(filter);
                if (!org.springframework.util.ObjectUtils.isEmpty(lookup)){
                    milestoneStatus.setLookupCode(lookup.getLookupCode());
                    milestoneStatus.setLookupId(lookup.getId());
                    milestoneStatus.setActivated(Boolean.TRUE);
                    listMilestoneStatusConfig.add(milestoneStatus);
                }
            }
        } else {
            MilestoneStatusConfigDTO dto = new MilestoneStatusConfigDTO();
            filter.setLookupCode(AppConstants.MilestoneStatus.COMPLETE);
            Lookup lookup = lookupService.findByLookupCodeLookupTypeTenantId(filter);
            dto.setLookupId(lookup.getId());
            dto.setLookupCode(lookup.getLookupCode());
            dto.setActivated(Boolean.TRUE);
            listMilestoneStatusConfig.add(dto);
        }
        return listMilestoneStatusConfig;
    }

    @Override
    public void downloadExcel(HttpServletResponse response, Long currentTenantId, MilestoneFilter filter){
        List<Milestone> dataList = findByTenantId(currentTenantId, filter);
        for (Milestone milestone: dataList){
            milestone.setExternal(milestone.getIsInternal());
        }
        this.sortByMultiFields(filter.getSort(), dataList);
        List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_EXPORT);
        excelUtils.exportExcel(response, dataList, Milestone.class, template);
    }

    public void validateAndSaveExcelData(Long currentTenantId, Workbook workbook, Sheet sheet, String fileName, Map<Integer,
            StringBuilder> mapExcelError, Set<String> excelEmailErrors, UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Long timeStartReadingFile, UpdateUserProfileDTO userInfo) {
        List<MilestoneDTO> datalist = excelUtils.parseExcelToDto(MilestoneDTO.class, sheet, fileName, uploadTemplateHdrIdDto, mapExcelError, excelEmailErrors);
        String emailTemplateName = StringUtils.EMPTY;
        // check duplicate entries inside excel list
        checkDuplicatedEntries(datalist, mapExcelError, excelEmailErrors, currentTenantId);
        // check existed entries from Database
        checkValidDataEntries(datalist, mapExcelError, excelEmailErrors, currentTenantId);

        if (!excelEmailErrors.isEmpty() || !mapExcelError.isEmpty()) {
            emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);

        } else if (excelEmailErrors.isEmpty() && mapExcelError.isEmpty()) {
            saveExcelData(datalist, mapExcelError, currentTenantId, excelEmailErrors);

            //if there is error while saving
            if (!mapExcelError.isEmpty() || !excelEmailErrors.isEmpty()) {
                emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.fail", null);
            } else {
                emailTemplateName = messagesUtilities.getMessageWithParam("email.template.upload.excel.setting.success", null);
            }
        }

        byte[] attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, uploadTemplateHdrIdDto, timeStartReadingFile);

        // Send email after uploading
        StringBuilder excelEmailErrorsVl = new StringBuilder();
        excelEmailErrors.forEach(cellError -> {
            excelEmailErrorsVl.append(cellError);
        });
        // Send upload success email
        excelUtils.sendNotificationEmail(emailTemplateName, messagesUtilities.getMessageWithParam("menu.label.milestones", null),
                excelEmailErrorsVl.toString(), attachedErrorFileByteArr, fileName, userInfo);
    }
}
