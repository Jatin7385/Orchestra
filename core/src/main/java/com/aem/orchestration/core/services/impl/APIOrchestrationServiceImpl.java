package com.aem.orchestration.core.services.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.orchestration.core.constants.FrameworkConstants;
import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;
import com.aem.orchestration.core.services.APIOrchestrationConfigService;
import com.aem.orchestration.core.services.APIOrchestrationService;
import com.aem.orchestration.core.services.apiAdapterService.APIAdapterService;
import com.aem.orchestration.core.services.apiService.ApiService;
import com.aem.orchestration.core.services.validation.ValidationService;

/**
 * Implementation of the API Orchestration Service.
 * 
 * This service orchestrates the execution of multiple API calls based on configuration.
 * For each configured API, it:
 * 1. Invokes the corresponding ApiService
 * 2. Validates the response using the configured ValidationService
 * 3. Formats the response using the configured APIAdapterService
 * 4. Merges results into the final response
 */
@Component(service = APIOrchestrationService.class, immediate = true)
public class APIOrchestrationServiceImpl implements APIOrchestrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIOrchestrationServiceImpl.class);

    @Reference
    private APIOrchestrationConfigService config;

    private JSONObject responseSchema;

    /** Dynamic map of registered API services */
    @Reference(
        service = ApiService.class,
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.MULTIPLE,
        bind = "registerAPIServiceHandler",
        unbind = "unbindAPIServiceHandler"
    )
    private final Map<String, ApiService> apiServiceHandlerMap = new HashMap<>();

    /** Dynamic map of registered validation services */
    @Reference(
        service = ValidationService.class,
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.MULTIPLE,
        bind = "registerValidationServiceHandler",
        unbind = "unbindValidationServiceHandler"
    )
    private final Map<String, ValidationService> validationServiceHandlerMap = new HashMap<>();

    /** Dynamic map of registered adapter services */
    @Reference(
        service = APIAdapterService.class,
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.MULTIPLE,
        bind = "registerAPIAdapterServiceHandler",
        unbind = "unbindAPIAdapterServiceHandler"
    )
    private final Map<String, APIAdapterService> apiAdapterServiceHandlerMap = new HashMap<>();

    @Override
    public JSONObject doProcess(SlingHttpServletRequest request, SlingHttpServletResponse response,
            JSONObject reqStringJson, JSONObject parametersJson, JSONArray auditInfoArray,
            String journeyName, String journeyId)
            throws IOException, ValidationException, JSONException, NoSuchAlgorithmException,
                   ParseException, OrchestrationException {
        
        LOGGER.info("APIOrchestrationServiceImpl :: doProcess start : {} : {}", journeyName, journeyId);

        // Get the response schema for this journey
        Map<String, String> responseSchemaMap = config.getAPIResponseSchema();
        if (responseSchemaMap.containsKey(journeyName)) {
            responseSchema = new JSONObject(responseSchemaMap.get(journeyName));
        } else if (responseSchemaMap.containsKey(FrameworkConstants.DEFAULT)) {
            responseSchema = new JSONObject(responseSchemaMap.get(FrameworkConstants.DEFAULT));
        } else {
            throw new OrchestrationException(
                FrameworkConstants.ERR_CONFIG_NOT_FOUND,
                "No API Response Schema configuration found for journey: " + journeyName,
                journeyName
            );
        }

        JSONObject finalResponseContainer = processAndValidateRegisteredApis(
            request, response, reqStringJson, parametersJson, auditInfoArray, journeyName, journeyId
        );

        LOGGER.info("APIOrchestrationServiceImpl :: doProcess end : {} : {}", journeyName, journeyId);
        return finalResponseContainer;
    }

    /**
     * Processes all configured APIs for the given journey and scenario.
     */
    private JSONObject processAndValidateRegisteredApis(
            SlingHttpServletRequest request, SlingHttpServletResponse response,
            JSONObject reqStringJson, JSONObject parametersJson,
            JSONArray auditInfoArray, String journeyName, String journeyId)
            throws IOException, JSONException, OrchestrationException, ValidationException,
                   NoSuchAlgorithmException, ParseException {

        LOGGER.info("APIOrchestrationServiceImpl :: processAndValidateRegisteredApis start : {} : {}", 
                    journeyName, journeyId);

        JSONObject finalResponse = new JSONObject();
        JSONObject finalResponseContainer = new JSONObject();
        JSONObject original = new JSONObject();
        String traceNumber = generateTraceNumber();
        String scenario = parametersJson.optString(FrameworkConstants.SCENARIO);
        
        String[] apis = getApisForJourneyAndScenario(journeyName, journeyId, scenario);

        for (String serviceName : apis) {
            // Get and invoke the ApiService
            ApiService service = getApiService(journeyName, journeyId, serviceName);
            JSONObject apiResponse = service.doProcess(
                request, response, auditInfoArray, parametersJson, finalResponse, reqStringJson, traceNumber
            );

            // Get and invoke the ValidationService
            ValidationService valService = getValidationService(journeyName, journeyId, scenario, serviceName);
            JSONObject validatedResponse = valService.doValidate(
                request, response, parametersJson, apiResponse, auditInfoArray, traceNumber
            );

            // Store original response
            original.put(serviceName, validatedResponse);

            // Get and invoke the AdapterService
            APIAdapterService adapterService = getAdapterService(journeyName, journeyId, scenario);
            JSONObject adaptedResponse = adapterService.format(
                request, response, validatedResponse, responseSchema, journeyName, journeyId
            );

            // Merge adapted response into final response
            mergeJSONObjects(finalResponse, adaptedResponse);
        }

        finalResponseContainer.put(FrameworkConstants.ORIGINAL, original);
        finalResponseContainer.put(FrameworkConstants.DATA, finalResponse);

        LOGGER.info("APIOrchestrationServiceImpl :: processAndValidateRegisteredApis end : {} : {}", 
                    journeyName, journeyId);
        return finalResponseContainer;
    }

    /**
     * Merges source JSON object into target (source overwrites target for same keys).
     */
    private void mergeJSONObjects(JSONObject target, JSONObject source) throws JSONException {
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            target.put(key, source.get(key));
        }
    }

    /**
     * Gets the list of API services to call for the given journey and scenario.
     */
    private String[] getApisForJourneyAndScenario(String journeyName, String journeyId, String scenario)
            throws OrchestrationException {
        LOGGER.info("APIOrchestrationServiceImpl :: getApisForJourneyAndScenario : {} : {} : {}", 
                    journeyName, journeyId, scenario);
        
        Map<String, String[]> apiList = config.getAPIOrchestrationConfig();
        String key = String.join(FrameworkConstants.PIPE, journeyName, scenario);

        if (!apiList.containsKey(key)) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_CONFIG_NOT_FOUND,
                "No API configuration found for journey: " + journeyName + ", scenario: " + scenario,
                ""
            );
        }

        return apiList.get(key);
    }

    /**
     * Gets the validation service name for the given context.
     */
    private String getValidationServiceName(String journeyName, String journeyId, String scenario, String serviceName) {
        Map<String, String> validationServiceMap = config.getValidationServiceConfig();
        String key = String.join(FrameworkConstants.PIPE, journeyName, scenario, serviceName);
        return validationServiceMap.get(key);
    }

    /**
     * Gets the adapter service name for the given context.
     */
    private String getAdapterServiceName(String journeyName, String journeyId, String scenario) {
        Map<String, String> adapterServiceMap = config.getAPIAdapterServiceConfig();
        String key = String.join(FrameworkConstants.PIPE, journeyName, scenario);
        return adapterServiceMap.get(key);
    }

    // ===================== Service Handler Registration =====================

    private void registerAPIServiceHandler(ApiService service, Map<?, ?> props) {
        String name = props.get(FrameworkConstants.NAME).toString();
        LOGGER.info(FrameworkConstants.REGISTER_HANDLER_LOG, name);
        apiServiceHandlerMap.put(name, service);
    }

    private void unbindAPIServiceHandler(ApiService service, Map<?, ?> props) {
        String name = props.get(FrameworkConstants.NAME).toString();
        LOGGER.info(FrameworkConstants.UNREGISTER_HANDLER_LOG, name);
        apiServiceHandlerMap.remove(name);
    }

    private void registerValidationServiceHandler(ValidationService service, Map<?, ?> props) {
        String name = props.get(FrameworkConstants.NAME).toString();
        LOGGER.info(FrameworkConstants.REGISTER_HANDLER_LOG, name);
        validationServiceHandlerMap.put(name, service);
    }

    private void unbindValidationServiceHandler(ValidationService service, Map<?, ?> props) {
        String name = props.get(FrameworkConstants.NAME).toString();
        LOGGER.info(FrameworkConstants.UNREGISTER_HANDLER_LOG, name);
        validationServiceHandlerMap.remove(name);
    }

    private void registerAPIAdapterServiceHandler(APIAdapterService service, Map<?, ?> props) {
        String name = props.get(FrameworkConstants.NAME).toString();
        LOGGER.info(FrameworkConstants.REGISTER_HANDLER_LOG, name);
        apiAdapterServiceHandlerMap.put(name, service);
    }

    private void unbindAPIAdapterServiceHandler(APIAdapterService service, Map<?, ?> props) {
        String name = props.get(FrameworkConstants.NAME).toString();
        LOGGER.info(FrameworkConstants.UNREGISTER_HANDLER_LOG, name);
        apiAdapterServiceHandlerMap.remove(name);
    }

    // ===================== Service Lookup Methods =====================

    private ApiService getApiService(String journeyName, String journeyId, String serviceName) 
            throws OrchestrationException {
        ApiService service = apiServiceHandlerMap.get(serviceName);
        if (service == null) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_API_SERVICE_NOT_FOUND,
                "ApiService not found: " + serviceName,
                ""
            );
        }
        return service;
    }

    private ValidationService getValidationService(String journeyName, String journeyId, 
            String scenario, String serviceName) throws OrchestrationException {
        
        if (StringUtils.isBlank(serviceName)) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_VALIDATION_FAILED,
                "Service name is empty or null",
                serviceName
            );
        }

        ValidationService handler = null;

        // First try to get by configured mapping
        String configKey = getValidationServiceName(journeyName, journeyId, scenario, serviceName);
        if (configKey != null && validationServiceHandlerMap.containsKey(configKey)) {
            handler = validationServiceHandlerMap.get(configKey);
        }

        // Fallback to service name directly
        if (handler == null) {
            handler = validationServiceHandlerMap.get(serviceName);
        }

        if (handler == null) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_VALIDATION_SERVICE_NOT_FOUND,
                "ValidationService not found for: " + serviceName,
                serviceName
            );
        }

        return handler;
    }

    private APIAdapterService getAdapterService(String journeyName, String journeyId, String scenario)
            throws OrchestrationException {
        
        String key = getAdapterServiceName(journeyName, journeyId, scenario);
        if (key == null) {
            key = FrameworkConstants.DEFAULT_ADAPTER_SERVICE;
        }

        APIAdapterService handler = apiAdapterServiceHandlerMap.get(key);
        if (handler == null) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_ADAPTER_SERVICE_NOT_FOUND,
                "APIAdapterService not found: " + key,
                scenario
            );
        }

        return handler;
    }

    /**
     * Generates a unique trace number for request tracking.
     */
    private String generateTraceNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 8);
    }
}
