package com.aem.orchestration.core.services.validation;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;

/**
 * Interface for Validation Service implementations in the Orchestration Framework.
 * 
 * Validation services are responsible for:
 * - Validating API responses against business rules
 * - Transforming or enriching response data as needed
 * - Throwing ValidationException if validation fails
 * 
 * Implementations should be OSGi components with a unique "name" property
 * that identifies them for dynamic service lookup.
 * 
 * Example registration:
 * <pre>
 * {@code @Component(service = ValidationService.class, property = {"name=my-validation-service"})}
 * public class MyValidationServiceImpl implements ValidationService { ... }
 * </pre>
 */
public interface ValidationService {

    /**
     * Validates an API response.
     * 
     * @param request          The Sling HTTP request
     * @param response         The Sling HTTP response
     * @param parametersJson   Flattened parameters from the request
     * @param responseAsJson   The API response to validate
     * @param auditInfoArray   Array for collecting audit information
     * @param traceNumber      Unique trace number for this request
     * @return JSONObject containing the validated (possibly transformed) response
     * @throws JSONException          If JSON processing fails
     * @throws OrchestrationException If an orchestration error occurs
     * @throws ValidationException    If validation fails
     */
    JSONObject doValidate(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONObject parametersJson,
            JSONObject responseAsJson,
            JSONArray auditInfoArray,
            String traceNumber
    ) throws JSONException, OrchestrationException, ValidationException;
}
