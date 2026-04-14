package com.aem.orchestration.core.services;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;

/**
 * Main orchestration service interface.
 * 
 * This service is the entry point for processing API requests through
 * the orchestration framework. It:
 * - Reads configuration to determine which APIs to call
 * - Invokes API services in sequence
 * - Applies validation services to each response
 * - Uses adapter services to format responses
 * - Aggregates results into a final response
 */
public interface APIOrchestrationService {

    /**
     * Processes a request through the orchestration pipeline.
     * 
     * @param request              The Sling HTTP request
     * @param response             The Sling HTTP response
     * @param reqStringJson        The parsed request JSON (context + payload)
     * @param parametersJson       Flattened parameters from the request
     * @param auditInfoArray       Array for collecting audit information
     * @param journeyName          The name of the journey being processed
     * @param journeyId            The unique ID for this journey instance
     * @return JSONObject containing the orchestrated response with "data" and "original" keys
     * @throws IOException              If I/O error occurs
     * @throws ValidationException      If validation fails
     * @throws JSONException            If JSON processing fails
     * @throws NoSuchAlgorithmException If a required algorithm is unavailable
     * @throws ParseException           If date/time parsing fails
     * @throws OrchestrationException   If an orchestration error occurs
     */
    JSONObject doProcess(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONObject reqStringJson,
            JSONObject parametersJson,
            JSONArray auditInfoArray,
            String journeyName,
            String journeyId
    ) throws IOException, ValidationException, JSONException, NoSuchAlgorithmException, 
             ParseException, OrchestrationException;
}
