package com.aem.orchestration.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi Configuration interface for API Orchestration Framework.
 * 
 * This configuration defines:
 * - API orchestration mappings (which APIs to call for which journey/scenario)
 * - Validation service mappings
 * - Adapter service mappings
 * - Response schema definitions
 */
@ObjectClassDefinition(
    name = "API Orchestration Configuration",
    description = "Configurations for the API Orchestration Framework"
)
public @interface APIOrchestrationConfig {

    /**
     * Gets the API Orchestration Config.
     * 
     * Format: JOURNEY_NAME|SCENARIO=API_NAME_1|API_NAME_2|...
     * 
     * Example: "USER_JOURNEY|login=customer-identification|otp-generation"
     * This means when journeyName=USER_JOURNEY and scenario=login,
     * call customer-identification first, then otp-generation.
     * 
     * @return Array of API orchestration configuration strings
     */
    @AttributeDefinition(
        name = "API Orchestration Config",
        description = "Enter: JOURNEY_NAME|SCENARIO=API_NAME_1|API_NAME_2 (e.g., USER_JOURNEY|login=customer-identification|otp-generation)",
        type = AttributeType.STRING
    )
    String[] getAPIOrchestrationConfig() default {
        "SAMPLE_JOURNEY|login=sample-api-service"
    };

    /**
     * Gets the Validation Service Config.
     * 
     * Format: JOURNEY_NAME|SCENARIO|API_SERVICE_NAME=VALIDATION_SERVICE_NAME
     * 
     * Example: "USER_JOURNEY|login|customer-identification=customer-validation"
     * This means for customer-identification API in login scenario,
     * use the customer-validation service to validate the response.
     * 
     * @return Array of validation service configuration strings
     */
    @AttributeDefinition(
        name = "Validation Service Config",
        description = "Enter: JOURNEY_NAME|SCENARIO|API_SERVICE_NAME=VALIDATION_SERVICE_NAME",
        type = AttributeType.STRING
    )
    String[] getValidationServiceConfig() default {
        "SAMPLE_JOURNEY|login|sample-api-service=sample-validation-service"
    };

    /**
     * Gets the API Adapter Service Config.
     * 
     * Format: JOURNEY_NAME|SCENARIO=ADAPTER_SERVICE_NAME
     * 
     * Example: "USER_JOURNEY|login=custom-adapter-service"
     * This means for login scenario, use custom-adapter-service to format responses.
     * If not specified, default-adapter-service is used.
     * 
     * @return Array of adapter service configuration strings
     */
    @AttributeDefinition(
        name = "API Adapter Service Config",
        description = "Enter: JOURNEY_NAME|SCENARIO=ADAPTER_SERVICE_NAME",
        type = AttributeType.STRING
    )
    String[] getAPIAdapterServiceConfig() default {};

    /**
     * Gets the API Response Schema Config.
     * 
     * Format: JOURNEY_NAME={"outputField": "sourceField.path", ...}
     * 
     * The schema maps output field names to paths in the API response.
     * Supports:
     * - Simple field mapping: "customerId": "response.customer.id"
     * - Array extraction: "accountNumbers": "accounts[*].number"
     * 
     * @return Array of response schema configuration strings
     */
    @AttributeDefinition(
        name = "API Response Schema Config",
        description = "Enter: JOURNEY_NAME={JSON schema mapping output fields to source paths}",
        type = AttributeType.STRING
    )
    String[] getAPIResponseSchema() default {
        "DEFAULT={\"status\": \"status\", \"message\": \"message\", \"data\": \"data\"}"
    };
}
