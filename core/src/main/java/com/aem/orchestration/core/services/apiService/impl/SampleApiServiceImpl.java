package com.aem.orchestration.core.services.apiService.impl;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.services.apiService.ApiService;

/**
 * Sample implementation of ApiService.
 * 
 * This demonstrates how to create an API service implementation.
 * In a real implementation, this would:
 * - Build the request payload for an external API
 * - Make the API call (e.g., via HTTP client, FDM, etc.)
 * - Process and return the response
 * 
 * The "name" property is used by the orchestration framework to look up this service.
 */
@Component(
    service = ApiService.class,
    property = {"name=sample-api-service"}
)
public class SampleApiServiceImpl implements ApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleApiServiceImpl.class);

    @Override
    public JSONObject doProcess(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONArray auditInfoArray,
            JSONObject parametersJson,
            JSONObject preProcessPayload,
            JSONObject reqStringJson,
            String traceNumber)
            throws OrchestrationException, JSONException, ParseException, NoSuchAlgorithmException {

        LOGGER.info("SampleApiServiceImpl :: doProcess start :: traceNumber: {}", traceNumber);

        // Example: Build and execute an API call
        JSONObject apiResponse = new JSONObject();
        
        try {
            // In a real implementation, you would:
            // 1. Extract necessary parameters from parametersJson
            String journeyName = parametersJson.optString("journeyName");
            String journeyId = parametersJson.optString("journeyID");
            
            // 2. Build the request payload for your external API
            // JSONObject apiRequest = buildApiRequest(parametersJson);
            
            // 3. Make the actual API call
            // For example, using HTTP client, FDM bridge, or other integration
            // JSONObject rawResponse = makeApiCall(apiRequest);
            
            // 4. Process the response
            // For this sample, we return a mock response
            apiResponse.put("status", "success");
            apiResponse.put("message", "Sample API call completed successfully");
            apiResponse.put("journeyName", journeyName);
            apiResponse.put("journeyId", journeyId);
            apiResponse.put("traceNumber", traceNumber);
            
            // Add sample data
            JSONObject data = new JSONObject();
            data.put("customerId", "CUST-12345");
            data.put("customerName", "John Doe");
            data.put("accountStatus", "ACTIVE");
            apiResponse.put("data", data);
            
            LOGGER.info("SampleApiServiceImpl :: doProcess :: API call successful for journey: {}", journeyName);
            
        } catch (JSONException e) {
            LOGGER.error("SampleApiServiceImpl :: doProcess :: Error processing API: {}", e.getMessage());
            throw new OrchestrationException("API_ERROR", "Error in sample API service: " + e.getMessage(), "sample-api-service");
        }

        LOGGER.info("SampleApiServiceImpl :: doProcess end :: traceNumber: {}", traceNumber);
        return apiResponse;
    }
}
