package com.aem.orchestration.core.services;

import java.util.Map;

/**
 * Service interface for retrieving API orchestration-related configurations.
 * 
 * This service provides access to the configuration maps that control:
 * - Which APIs to call for each journey/scenario
 * - Which validation services to use
 * - Which adapter services to use
 * - Response schema definitions
 */
public interface APIOrchestrationConfigService {

    /**
     * Retrieves the API orchestration configuration map.
     * 
     * Key: "JOURNEY_NAME|SCENARIO"
     * Value: Array of API service names to call in sequence
     * 
     * @return Map of journey|scenario to array of API service names
     */
    Map<String, String[]> getAPIOrchestrationConfig();

    /**
     * Retrieves the validation service configuration map.
     * 
     * Key: "JOURNEY_NAME|SCENARIO|API_SERVICE_NAME"
     * Value: Validation service name to use
     * 
     * @return Map of journey|scenario|api to validation service name
     */
    Map<String, String> getValidationServiceConfig();

    /**
     * Retrieves the adapter service configuration map.
     * 
     * Key: "JOURNEY_NAME|SCENARIO"
     * Value: Adapter service name to use
     * 
     * @return Map of journey|scenario to adapter service name
     */
    Map<String, String> getAPIAdapterServiceConfig();

    /**
     * Retrieves the API response schema configuration map.
     * 
     * Key: Journey name (or "DEFAULT" for fallback)
     * Value: JSON string defining the response schema
     * 
     * @return Map of journey name to schema JSON string
     */
    Map<String, String> getAPIResponseSchema();
}
