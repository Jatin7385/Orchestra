package com.aem.orchestration.core.services.validation.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;
import com.aem.orchestration.core.services.validation.ValidationService;

/**
 * Sample implementation of ValidationService.
 * 
 * This demonstrates how to create a validation service implementation.
 * In a real implementation, this would:
 * - Validate the API response against business rules
 * - Check for error conditions
 * - Optionally transform or enrich the response
 * 
 * The "name" property is used by the orchestration framework to look up this service.
 */
@Component(
    service = ValidationService.class,
    property = {"name=sample-validation-service"}
)
public class SampleValidationServiceImpl implements ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleValidationServiceImpl.class);

    @Override
    public JSONObject doValidate(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONObject parametersJson,
            JSONObject responseAsJson,
            JSONArray auditInfoArray,
            String traceNumber)
            throws JSONException, OrchestrationException, ValidationException {

        LOGGER.info("SampleValidationServiceImpl :: doValidate start :: traceNumber: {}", traceNumber);

        // Example validation logic
        
        // 1. Check if response is null or empty
        if (responseAsJson == null || responseAsJson.length() == 0) {
            throw new ValidationException(
                "VAL_001",
                "API response is null or empty",
                null,
                "sample-validation-service"
            );
        }

        // 2. Check for error status in response
        String status = responseAsJson.optString("status", "");
        if ("error".equalsIgnoreCase(status)) {
            String errorMessage = responseAsJson.optString("message", "Unknown error");
            throw new ValidationException(
                "VAL_002",
                "API returned error status: " + errorMessage,
                null,
                "sample-validation-service"
            );
        }

        // 3. Validate required fields
        // Example: check if customerId exists in the data
        JSONObject data = responseAsJson.optJSONObject("data");
        if (data != null) {
            String customerId = data.optString("customerId", "");
            if (customerId.isEmpty()) {
                LOGGER.warn("SampleValidationServiceImpl :: customerId is empty in response");
                // Depending on business logic, you might throw an exception or just log
            }
        }

        // 4. Optionally enrich or transform the response
        // For example, add a validation timestamp
        JSONObject validatedResponse = new JSONObject(responseAsJson.toString());
        validatedResponse.put("validationStatus", "VALIDATED");
        validatedResponse.put("validatedBy", "sample-validation-service");

        LOGGER.info("SampleValidationServiceImpl :: doValidate end :: traceNumber: {}", traceNumber);
        return validatedResponse;
    }
}
