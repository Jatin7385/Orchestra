package com.aem.orchestration.core.services.apiService;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aem.orchestration.core.exceptions.OrchestrationException;

/**
 * Interface for API Service implementations in the Orchestration Framework.
 * 
 * Each API service handles a specific API call, including:
 * - Building the request payload
 * - Making the actual API call (e.g., via FDM, REST, etc.)
 * - Processing the response
 * 
 * Implementations should be OSGi components with a unique "name" property
 * that identifies them for dynamic service lookup.
 * 
 * Example registration:
 * <pre>
 * {@code @Component(service = ApiService.class, property = {"name=my-api-service"})}
 * public class MyApiServiceImpl implements ApiService { ... }
 * </pre>
 */
public interface ApiService {

    /**
     * Processes an API call within the orchestration flow.
     * 
     * @param request              The Sling HTTP request
     * @param response             The Sling HTTP response
     * @param auditInfoArray       Array for collecting audit information
     * @param parametersJson       Flattened parameters from the request
     * @param preProcessPayload    Aggregated response from previous API calls
     * @param reqStringJson        Original request string JSON
     * @param traceNumber          Unique trace number for this request
     * @return JSONObject containing the API response
     * @throws OrchestrationException   If an orchestration error occurs
     * @throws JSONException            If JSON processing fails
     * @throws ParseException           If date/time parsing fails
     * @throws NoSuchAlgorithmException If a required algorithm is unavailable
     */
    JSONObject doProcess(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONArray auditInfoArray,
            JSONObject parametersJson,
            JSONObject preProcessPayload,
            JSONObject reqStringJson,
            String traceNumber
    ) throws OrchestrationException, JSONException, ParseException, NoSuchAlgorithmException;
}
