package com.y3technologies.masters.constants;

import java.util.HashMap;
import java.util.Map;

public class AppConstants {

	public static final class ProfileCode {
		public static final String CUSTOMER = "CUSTOMER";
		public static final String TRANSPORTER = "TRANSPORTER";
		public static final String LOCATION = "LOCATION";
		public static final String CONSIGNEE = "CONSIGNEE";
	}

	public static final class PartnerType {
		public static final String CUSTOMER = "CUSTOMER";
		public static final String TRANSPORTER = "TRANSPORTER";
		public static final String CONSIGNEE = "CONSIGNEE";
	}

	public static final class LookupType {
		public static final String PARTNER_TYPES = "PartnerTypes";
		public static final String LOCATION_TAG = "LocationTag";
		public static final String UOM_GROUP = "UOMGroup";
		public static final String MILESTONE_UPDATE_STATUS_TYPE = "MilestoneUpdateStatusType";
		public static final String EQUIPMENT_TYPE = "EquipmentType";
		public static final String EQUIPMENT_UNIT_TYPE = "EquipmentUnitType";
		public static final String DRIVER_LICENSE_TYPE = "DriverLicenseType";
		public static final String VEHICLE_LICENSE_CLASS = "VehicleLicenceClass";
		public static final String VEHICLE_TYPE = "VehicleType";
	}

	public static final class CommonTag {
		public static final String LOCATION_TAG = "LocationTag";
	}

	public static final class MilestoneStatus {
		public static final String PARTIAL = "PARTIAL";
		public static final String COMPLETE = "COMPLETE";
		public static final String FAILED = "FAILED";
	}

	public static final class Milestone {
		public static final String ORDER_RECEIVED = "ORDER_RECEIVED";
		public static final String PLANNED_WITH_TRANSPORTER = "PLANNED_WITH_TRANSPORTER";
		public static final String PLANNED_WITH_VEHICLE_DRIVER = "PLANNED_WITH_VEHICLE_DRIVER";
		public static final String UNASSIGN_TRANSPORTER = "UNASSIGN_TRANSPORTER";
		public static final String UNASSIGN_VEHICLE_DRIVER = "UNASSIGN_VEHICLE_DRIVER";
		public static final String UPDATED_TRANSPORTER = "UPDATED_TRANSPORTER";
		public static final String UPDATED_VEHICLE_DRIVER = "UPDATED_VEHICLE_DRIVER";
		public static final String REQUEST_DISPATCHED = "REQUEST_DISPATCHED";
		public static final String ARRIVE_FOR_PICK_UP = "ARRIVE_FOR_PICK_UP";
		public static final String PICK_UP = "PICK_UP";
		public static final String ARRIVE_FOR_DROP_OFF = "ARRIVE_FOR_DROP_OFF";
		public static final String DROP_OFF = "DROP_OFF";
		public static final String POD_RETURN_TO_WAREHOUSE = "POD_RETURN_TO_WAREHOUSE";
		public static final String POD_RETURN_TO_CUSTOMER = "POD_RETURN_TO_CUSTOMER";
	}

	public static final class TptRequestStatus {
		public static final String NEW = "NEW";
		public static final String PLAN = "PLAN";
		public static final String DISPATCH = "DISPATCH";
		public static final String PICK_UP = "PICK_UP";
		public static final String COMPLETE = "COMPLETE";
		public static final String PARTIAL = "PARTIAL";
		public static final String FAILED = "FAILED";
	}

	public static final class ExcelTemplateCodes{
		public static final String CONFIGCODE_SETTING_PAGE = "CONFIGURATION SETTING";
		public static final String CONFIGCODE_SETTING_UPLOAD = "CONFIGCODE_SETTING_UPLOAD";
		public static final String CONFIGCODE_SETTING_EXPORT= "CONFIGCODE_SETTING_EXPORT";

		public static final String CONSIGNEE_SETTING_PAGE = "CONSIGNEE SETTING";
		public static final String CONSIGNEE_SETTING_UPLOAD = "CONSIGNEE_SETTING_UPLOAD";
		public static final String CONSIGNEE_SETTING_EXPORT = "CONSIGNEE_SETTING_EXPORT";

		public static final String CUSTOMER_SETTING_PAGE = "CUSTOMER SETTING";
		public static final String CUSTOMER_SETTING_UPLOAD = "CUSTOMER_SETTING_UPLOAD";
		public static final String CUSTOMER_SETTING_EXPORT= "CUSTOMER_SETTING_EXPORT";

		public static final String DRIVER_SETTING_PAGE = "DRIVER SETTING";
		public static final String DRIVER_SETTING_UPLOAD = "DRIVER_SETTING_UPLOAD";
		public static final String DRIVER_SETTING_EXPORT = "DRIVER_SETTING_EXPORT";

		public static final String EQUIPMENT_SETTING_PAGE = "EQUIPMENT SETTING";
		public static final String EQUIPMENT_SETTING_UPLOAD = "EQUIPMENT_SETTING_UPLOAD";
		public static final String EQUIPMENT_SETTING_EXPORT= "EQUIPMENT_SETTING_EXPORT";

		public static final String LOCATION_SETTING_PAGE = "LOCATION SETTING";
		public static final String LOCATION_SETTING_UPLOAD = "LOCATION_SETTING_UPLOAD";
		public static final String LOCATION_SETTING_EXPORT = "LOCATION_SETTING_EXPORT";

		public static final String MILESTONE_SETTING_PAGE = "MILESTONE SETTING";
		public static final String MILESTONES_SETTING_EXPORT= "MILESTONES_SETTING_EXPORT";
		public static final String MILESTONES_SETTING_UPLOAD = "MILESTONES_SETTING_UPLOAD";

		public static final String REASONCODE_SETTING_PAGE = "REASON CODE SETTING";
		public static final String REASONCODE_SETTING_UPLOAD = "REASONCODE_SETTING_UPLOAD";
		public static final String REASONCODE_SETTING_EXPORT= "REASONCODE_SETTING_EXPORT";

		public static final String TRANSPORTER_SETTING_PAGE = "TRANSPORTER SETTING";
		public static final String TRANSPORTER_SETTING_UPLOAD = "TRANSPORTER_SETTING_UPLOAD";
		public static final String TRANSPORTER_SETTING_EXPORT = "TRANSPORTER_SETTING_EXPORT";

		public static final String UOM_SETTING_PAGE = "UOM SETTING";
		public static final String UOM_SETTING_UPLOAD = "UOM_SETTING_UPLOAD";
		public static final String UOM_SETTING_EXPORT = "UOM_SETTING_EXPORT";

		public static final String VEHICLE_SETTING_PAGE = "VEHICLE SETTING";
		public static final String VEHICLE_SETTING_UPLOAD = "VEHICLE_SETTING_UPLOAD";
		public static final String VEHICLE_SETTING_EXPORT= "VEHICLE_SETTING_EXPORT";

		public static final String LOOKUP_SETTING_PAGE = "LOOKUP SETTING";
		public static final String LOOKUP_SETTING_UPLOAD = "LOOKUP_SETTING_UPLOAD";
		public static final String LOOKUP_SETTING_EXPORT = "LOOKUP_SETTING_EXPORT";

		public static final String API_CONFIG_DOWNLOAD = "API_CONFIG_DOWNLOAD";

		public static final Integer ERROR_COLUMN_WIDTH = 9000;
    }

	public static final class usageLevel {
		public static final Map<String, String> mapUsageLevel = new HashMap<String, String>() {{
			put("ALL", "ALL");
			put("TENANT", "TENANT");
			put("PARTNERS", "PARTNERS");
		}};
	}

	public static final class Role {
		public static final String DRIVER = "Driver";
	}

	public static final String TPT_REQUEST_STATUS_TYPE = "TptRequestStatusType";

	public static final String PARTNER_TYPE = "PartnerTypes";

	public static final class ProfileScopeSettings {
		public static final String CUSTOMER = "Customer";
		public static final String TRANSPORTER = "Transporter";
		public static final String LOCATION = "Location";
		public static final String CONSIGNEE = "Consignee";
		public static final String DRIVER = "Driver";
		public static final String VEHICLE = "Vehicle";
		public static final String CONSIGNEE_PLACE = "Consignee Places";
	}

	public static final class CommonSortDirection {
		public static final String DESCENDING = "DESC";
		public static final String ASCENDING = "ASC";
	}

	public static final class CommonPropertyName {
		public static final String CREATED_DATE = "createdDate";
		public static final String ID = "id";
	}

	public static final class SortPropertyName {
		public static final String DRIVER_NAME = "name";
		public static final String DRIVER_TRANSPORTER_NAME = "partnerName";
		public static final String DRIVER_MOBILE_NUMBER = "mobileNumber1";
		public static final String DRIVER_LICENCE_TYPE = "licenceType";

		public static final String VEHICLE_REG_NUMBER = "vehicleRegNumber";
		public static final String VEHICLE_TYPE = "vehicleType";
		public static final String VEHICLE_TRANSPORTER_NAME = "partnerName";
		public static final String VEHICLE_LOCATION = "locName";
		public static final String VEHICLE_LICENCE_TYPE = "licenceTypeRequired";

		public static final String UOM_UOM = "uom";
		public static final String UOM_GROUP = "uomGroup";
		public static final String UOM_DESCRIPTION = "description";

		public static final String EQUIPMENT_UNITAIDC1 = "unitAidc1";
		public static final String EQUIPMENT_UNITAIDC2 = "unitAidc2";
		public static final String EQUIPMENT_TYPE = "equipmentType";
		public static final String EQUIPMENT_UNIT_TYPE = "unitType";

		public static final String MILESTONES_CODE = "milestoneCode";
		public static final String MILESTONES_DESCRIPTION = "milestoneDescription";
		public static final String MILESTONES_CATEGORY = "milestoneCategory";
		public static final String MILESTONES_CUSTOMIZED_DESCRIPTION = "customerDescription";
		public static final String MILESTONES_EXTERNAL = "isInternal";

		public static final String REASON_CODES_CODE = "reasonCode";
		public static final String REASON_CODES_DESCRIPTION = "reasonDescription";
		public static final String REASON_CODES_CATEGORY = "category";
		public static final String REASON_CODES_USAGE_LEVEL = "usage";
		public static final String REASON_CODES_INDUCED_BY = "inducedBy";

		public static final String CONFIG_CODE_CODE = "code";
		public static final String CONFIG_CODE_DESCRIPTION = "description";
		public static final String CONFIG_CODE_USAGE_LEVEL = "usageLevel";
		public static final String CONFIG_CODE_CONFIG_VALUE = "configValue";

		public static final String LOCATION_NAME = "locName";
		public static final String LOCATION_CODE = "locCode";
		public static final String LOCATION_TAG = "locationTag";
		public static final String LOCATION_ADDRESS = "address";
		public static final String LOCATION_ZIPCODE = "zipCode";

		public static final String CUSTOMER_NAME = "partnerName";
		public static final String CUSTOMER_CODE = "partnerCode";

		public static final String TRANSPORTER_NAME = "name";
		public static final String TRANSPORTER_CODE = "partnerCode";
		public static final String TRANSPORTER_DEDICATED_TO = "customerName";

		public static final String CONSIGNEE_NAME = "name";
		public static final String CONSIGNEE_CODE = "partnerCode";
		public static final String CONSIGNEE_ASSIGNED_TO = "customerName";
	}

	public static final class UserInfo {
		public static final Long DEFAULT_TENANT_ID = 0l;
	}

	public static final class UploadExcelTemplate {
		public static final String CONFIGCODE_SETTING = "CONFIGCODE_SETTING.xlsx";
		public static final String CONSIGNEE_SETTING = "CONSIGNEE_SETTING.xlsx";
		public static final String CUSTOMER_SETTING = "CUSTOMER_SETTING.xlsx";
		public static final String DRIVER_SETTING = "DRIVER_SETTING.xlsx";
		public static final String EQUIPMENT_SETTING = "EQUIPMENT_SETTING.xlsx";
		public static final String LOCATION_SETTING = "LOCATION_SETTING.xlsx";
		public static final String MILESTONE_SETTING = "MILESTONE_SETTING.xlsx";
		public static final String REASONCODE_SETTING = "REASONCODE_SETTING.xlsx";
		public static final String TRANSPORTER_SETTING = "TRANSPORTER_SETTING.xlsx";
		public static final String UOM_SETTING = "UOM_SETTING.xlsx";
		public static final String VEHICLE_SETTING = "VEHICLE_SETTING.xlsx";
		public static final String LOOKUP_SETTING = "LOOKUP_SETTING.xlsx";
	}

	public static final class UploadExcelResponse {
		public static final String UPLOADED_FILE = "UPLOADED_FILE";
		public static final String EMPTY_FILE = "EMPTY_FILE";
		public static final String INVALID_HEADERS = "INVALID_HEADERS";
		public static final String INVALID_FILE = "INVALID_FILE";
		public static final String INVALID_FILE_TYPE = "INVALID_FILE_TYPE";

		//folder contains uploaded excel file in ftp server
		public static final String UPLOADED_EXCEL_FOLDER = "UploadedExcel";

		public static final double AVG_RECORD_PROCESSING_TIME = 0.05;
	}

	public static final class DownloadTemplateCode {
		public static final String CONFIGCODE = "CONFIGCODE";
		public static final String CONSIGNEE = "CONSIGNEE";
		public static final String CUSTOMER = "CUSTOMER";
		public static final String DRIVER = "DRIVER";
		public static final String EQUIPMENT = "EQUIPMENT";
		public static final String LOCATION = "LOCATION";
		public static final String MILESTONE = "MILESTONE";
		public static final String REASONCODE = "REASONCODE";
		public static final String TRANSPORTER = "TRANSPORTER";
		public static final String UOM = "UOM";
		public static final String VEHICLE = "VEHICLE";
		public static final String LOOKUP = "LOOKUP";
	}

	public static final class MastersConstants {
		public static final Integer CORE_POOL_SIZE = 4;
		public static final Integer MAX_POOL_SIZE = 20;
		public static final Integer QUEUE_CAPACITY = 500;
		public static final String THREAD_NAME_PREFIX = "masters-thread-pool";
	}

	public static final String FTP_DEFAULT_FOLDER_NAME = "default";
}
