package com.aem.orchestration.core.services.apiAdapterService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;

/**
 * Interface for API Adapter Service implementations in the Orchestration Framework.
 * 
 * Adapter services are responsible for:
 * - Transforming API responses to a standardized format
 * - Mapping fields from the raw API response to the expected schema
 * - Providing consistent response structure across different APIs
 * 
 * Implementations should be OSGi components with a unique "name" property
 * that identifies them for dynamic service lookup.
 * 
 * Example registration:
 * <pre>
 * {@code @Component(service = APIAdapterService.class, property = {"name=my-adapter-service"})}
 * public class MyAdapterServiceImpl implements APIAdapterService { ... }
 * </pre>
 */
public interface APIAdapterService {

    /**
     * Formats an API response according to a defined schema.
     * 
     * @param request          The Sling HTTP request
     * @param response         The Sling HTTP response
     * @param apiResponse      The raw API response to format
     * @param schemaJSONObject The schema defining the output format
     * @param journeyName      The name of the current journey
     * @param journeyId        The ID of the current journey
     * @return JSONObject containing the formatted response
     * @throws IOException              If I/O error occurs
     * @throws ValidationException      If validation fails during formatting
     * @throws JSONException            If JSON processing fails
     * @throws NoSuchAlgorithmException If a required algorithm is unavailable
     * @throws ParseException           If date/time parsing fails
     * @throws OrchestrationException   If an orchestration error occurs
     */
    JSONObject format(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONObject apiResponse,
            JSONObject schemaJSONObject,
            String journeyName,
            String journeyId
    ) throws IOException, ValidationException, JSONException, NoSuchAlgorithmException, 
             ParseException, OrchestrationException;
}
