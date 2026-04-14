package com.aem.orchestration.core.constants;

import java.util.regex.Pattern;

/**
 * Framework constants used throughout the API Orchestration Framework.
 */
public final class FrameworkConstants {
    
    // General Constants
    public static final String DEFAULT = "DEFAULT";
    public static final String FORWARD_SLASH = "/";
    public static final String PIPE = "|";
    public static final String HYPHEN = "-";
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String NAME = "name";
    
    // Request/Response Keys
    public static final String DATA = "data";
    public static final String ORIGINAL = "original";
    public static final String RESPONSE = "response";
    public static final String STATUS = "status";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String ERROR_DETAILS = "errorDetails";
    
    // Request Structure Keys
    public static final String REQUEST_STRING = "requestString";
    public static final String CONTEXT = "context";
    public static final String PAYLOAD = "payload";
    public static final String JOURNEY_ID = "journeyID";
    public static final String JOURNEY_NAME = "journeyName";
    public static final String SCENARIO = "scenario";
    public static final String API_NAME = "apiName";
    
    // Default Values
    public static final String JOURNEY_ID_NOT_GENERATED = "journeyIdNotGenerated";
    public static final String JOURNEY_NAME_NOT_GENERATED = "journeyNameNotGenerated";
    public static final String API_NAME_NOT_PASSED = "apiNameNotPassed";
    public static final String SCENARIO_NOT_PASSED = "scenarioNotPassed";
    public static final String USER_AGENT_NOT_PASSED = "userAgentNotPassed";
    
    // Header Keys
    public static final String REQUEST_USER_AGENT = "User-Agent";
    public static final String USER_AGENT = "userAgent";
    
    // Adapter Service Constants
    public static final String DEFAULT_ADAPTER_SERVICE = "default-adapter-service";
    
    // Pattern for array notation in JSON paths
    public static final Pattern ARRAY_PRESENT_PATTERN = Pattern.compile("\\[(.*?)\\]");
    
    // Error Codes
    public static final String ERR_CONFIG_NOT_FOUND = "ERR_CFG_001";
    public static final String ERR_API_SERVICE_NOT_FOUND = "ERR_SVC_001";
    public static final String ERR_VALIDATION_SERVICE_NOT_FOUND = "ERR_SVC_002";
    public static final String ERR_ADAPTER_SERVICE_NOT_FOUND = "ERR_SVC_003";
    public static final String ERR_INVALID_REQUEST = "ERR_REQ_001";
    public static final String ERR_VALIDATION_FAILED = "ERR_VAL_001";
    
    // Logging Constants
    public static final String REGISTER_HANDLER_LOG = "Register Handler -- {}";
    public static final String UNREGISTER_HANDLER_LOG = "UnRegister Handler -- {}";
    public static final String MAP_DATA_LOG = "Map Data -- {}";
    
    // Status Values
    public static final String COMPLETED = "Completed";
    public static final String HAS_FAILED = "HasFailed";
    public static final String REQUESTED = "Requested";
    
    // Date Formats
    public static final String DATE_FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
    
    private FrameworkConstants() {
        // Private constructor to prevent instantiation
    }
}
