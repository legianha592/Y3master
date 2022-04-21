package com.y3technologies.masters.dto.filter;

import lombok.Data;

@Data
public class TptRequestReportFilter {

    private Long tenantId;

    private Long receivedDateFrom;

    private Long receivedDateTo;

    private String requestNo;

    private String tptRequestStatus;

    private String customerId;

    private String tptRequestType;

    private String transporterId;

    private String vehiclePlateNo;

    private String driverName;

    private String consignee;

    private String location;

    private String serviceLevel;

    private String salesChannel;

    private String serviceType;

    private String serviceTag;
}
