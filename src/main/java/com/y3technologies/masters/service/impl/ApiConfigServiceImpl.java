package com.y3technologies.masters.service.impl;

import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.apiConfig.AddConfigRequest;
import com.y3technologies.masters.dto.apiConfig.ApiConfigDTO;
import com.y3technologies.masters.dto.apiConfig.ApiConfigExcelDTO;
import com.y3technologies.masters.dto.apiConfig.ApiConfigFilter;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.apiConfig.ApiConfig;
import com.y3technologies.masters.model.apiConfig.ApiType;
import com.y3technologies.masters.model.apiConfig.AuthenticationType;
import com.y3technologies.masters.repository.ApiConfigRepository;
import com.y3technologies.masters.service.ApiConfigService;
import com.y3technologies.masters.util.EncryptUtils;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.RandomUtils;
import com.y3technologies.masters.util.RestPageImpl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApiConfigServiceImpl implements ApiConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ApiConfigServiceImpl.class);

    private final ApiConfigRepository apiConfigRepository;

    private final ExcelClient excelClient;

    private final ExcelUtils excelUtils;

    private final EncryptUtils encryptUtils;

    @Override
    public ApiConfig findById(Long id) {
        Optional<ApiConfig> apiConfigOptional = apiConfigRepository.findById(id);
        if (apiConfigOptional.isEmpty()) {
            return null;
        }
        return apiConfigOptional.get();
    }

    @Override
    public ApiConfig save(AddConfigRequest request, Lookup lookup, Partners customer, Long tenantId) {

        // get Api type
        ApiType type;
        if (lookup.getLookupDescription() != null
                && lookup.getLookupDescription().contains(ApiType.INCOMING.toString())) {
            // validate urn length for incoming api
            if (request.getUrn().length() > 100) {
                throw new TransactionException("exception.apiconfig.urn.invalid.length");
            }
            type = ApiType.INCOMING;
        } else if (lookup.getLookupDescription() != null
                && lookup.getLookupDescription().contains(ApiType.OUT_GOING.toString())) {
            type = ApiType.OUT_GOING;
        } else {
            throw new TransactionException("exception.apiconfig.apitype.invalid");
        }

        // init apiConfig entity
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setLookup(lookup);
        apiConfig.setCustomer(customer);
        apiConfig.setType(type);
        apiConfig.setUrl(request.getUrl());
        apiConfig.setUrn(request.getUrn());
        apiConfig.setDescription(request.getDescription());
        if (ApiType.INCOMING.equals(type)) {
            if (StringUtils.isEmpty(request.getUrn())) {
                throw new TransactionException("exception.apiConfig.urn.required");
            }
            if (StringUtils.isEmpty(request.getApiKey())) {
                apiConfig.setApiKey(generateApiKey());
            }else {
                apiConfig.setApiKey(request.getApiKey());
            }
        } else {
            if (StringUtils.isNotEmpty(request.getApiKey())) {
                apiConfig.setAuthenType(AuthenticationType.API_KEY);
                apiConfig.setApiKey(request.getApiKey());
            } else if (StringUtils.isNotEmpty(request.getUsername())
                    && StringUtils.isNotEmpty(request.getPassword())) {
                apiConfig.setAuthenType(AuthenticationType.USERNAME_PASSWORD);
                apiConfig.setUsername(request.getUsername());
                    // If has not yet encrypted => Encrypt
                    try {
                        apiConfig.setPassword(encryptUtils.encrypt(request.getPassword()));
                    } catch (Exception ex) {
                        throw new TransactionException("exception.apiconfig.encrypt.password.fail");
                    }
            } else {
                throw new TransactionException("exception.apiConfig.authen.type.not.supported");
            }
        }
        apiConfig.setIsActive(request.getIsActive());
        apiConfig.setTenantId(tenantId);

        return apiConfigRepository.save(apiConfig);
    }

    @Override
    public boolean existsByLookupAndCustomer(Lookup lookup, Partners customer) {
        return apiConfigRepository.existsByLookupAndCustomer(lookup, customer);
    }

    @Override
    public String generateApiKey() {
        while (true) {
            String newApiKey = RandomUtils.generateAlphaNumeric(32);
            if (!apiConfigRepository.existsByApiKey(newApiKey)) {
                return newApiKey;
            }
        }
    }

    @Override
    public ApiConfig update(ApiConfig config) {
        if (StringUtils.isEmpty(config.getUrl())) {
            throw new TransactionException("exception.apiConfig.url.required");
        }

        if (ApiType.INCOMING.equals(config.getType())) {
            if (StringUtils.isEmpty(config.getUrn())) {
                throw new TransactionException("exception.apiConfig.urn.required");
            }
            if (StringUtils.isEmpty(config.getApiKey())) {
                config.setApiKey(generateApiKey());
            }
        } else if (ApiType.OUT_GOING.equals(config.getType())) {
            if (StringUtils.isNotEmpty(config.getApiKey())) {
                config.setAuthenType(AuthenticationType.API_KEY);
            } else if (StringUtils.isNotEmpty(config.getUsername())
                    && StringUtils.isNotEmpty(config.getPassword())) {
                config.setAuthenType(AuthenticationType.USERNAME_PASSWORD);
                try {
                  // Check whether has been encrypted yet
                  encryptUtils.decrypt(config.getPassword());
                } catch(Exception e) {
                  // If has not yet encrypted => Encrypt
                  try {
                    config.setPassword(encryptUtils.encrypt(config.getPassword()));
                  } catch (Exception ex) {
                    throw new TransactionException("exception.apiconfig.encrypt.password.fail");
                  }
                }
            } else {
                throw new TransactionException("exception.apiConfig.authen.type.not.supported");
            }
        } else {
            throw new TransactionException("exception.apiConfig.api.type.not.supported");
        }

        return apiConfigRepository.save(config);
    }

    @Override
    public Page<ApiConfigDTO> findAllApiConfigWithPagination(ApiConfigFilter filter, Long tenantId) {

        Page<ApiConfig> listApiConfig = apiConfigRepository.findAllApiConfigWithPagination(filter, tenantId);

        List<ApiConfigDTO> listDto = listApiConfig.getContent().stream().map(this::mapToDto).collect(Collectors.toList());

        return new RestPageImpl<>(listDto
                , listApiConfig.getPageable()
                , listApiConfig.getTotalElements());
    }

    @Override
    public void downloadExcel(ApiConfigFilter filter, Long tenantId, HttpServletResponse response) {
        Page<ApiConfig> listApiConfig = apiConfigRepository.findAllApiConfigWithPagination(filter, tenantId);
        List<ApiConfigExcelDTO> listApiConfigDto = listApiConfig.getContent().stream().map(this::mapToDto).map((apiConfigDTO) -> {
          ApiConfigExcelDTO excelDTO = new ApiConfigExcelDTO();
          BeanUtils.copyProperties(apiConfigDTO, excelDTO);
          excelDTO.setActiveInd(apiConfigDTO.getIsActive());
          return excelDTO;
        }).collect(Collectors.toList());
        List<UploadTemplateHdrIdDto> template = excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.API_CONFIG_DOWNLOAD);
        excelUtils.exportExcel(response, listApiConfigDto, ApiConfigExcelDTO.class, template);
    }

    @Override
    public boolean checkExistApiConfig(ApiConfigFilter filter) {
        if (ObjectUtils.isEmpty(filter.getUrn())) {
            return false;
        }

        if (ObjectUtils.isNotEmpty(filter.getApiKey())) {
            return apiConfigRepository.existsByUrnAndApiKey(filter.getUrn(), filter.getApiKey());
        }

        if (ObjectUtils.isNotEmpty(filter.getUsername()) && ObjectUtils.isNotEmpty(filter.getPassword())) {
            try {
                return apiConfigRepository.existsByUrnAndUsernameAndPassword(
                        filter.getUrn(),
                        filter.getUsername(),
                        encryptUtils.encrypt(filter.getPassword())
                );
            } catch (Exception ex) {
                throw new TransactionException("exception.apiconfig.encrypt.password.fail");
            }
        }

        return false;
    }

    private ApiConfigDTO mapToDto(ApiConfig apiConfig) {
        ApiConfigDTO apiConfigDTO = new ApiConfigDTO();
        BeanUtils.copyProperties(apiConfig, apiConfigDTO);
        apiConfigDTO.setApiId(apiConfig.getLookup().getId());
        apiConfigDTO.setApiName(apiConfig.getLookup().getLookupCode());
        apiConfigDTO.setApiDesc(apiConfig.getDescription());
        apiConfigDTO.setCustomerName(apiConfig.getCustomer().getPartnerName());
        apiConfigDTO.setCustomerId(apiConfig.getCustomer().getId());
        if(StringUtils.isNotBlank(apiConfig.getPassword())) {
            try {
                apiConfigDTO.setPassword(encryptUtils.decrypt(apiConfig.getPassword()));
            } catch(Exception e) {
                logger.error(e.getMessage());
            }
        }
        return apiConfigDTO;
    }
}
