package com.aem.orchestration.core.exceptions;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ValidationException is a custom exception class for validation-related errors
 * in the API Orchestration Framework.
 * 
 * This exception captures:
 * - errorCode: unique identifier for the validation error type
 * - errorMessage: description of the validation failure
 * - additionalParameters: extra context for debugging
 * - apiName: the API that triggered the exception
 * 
 * It provides a method to generate a structured JSON error response.
 */
public class ValidationException extends Exception implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationException.class);
    
    private final String errorCode;
    private final String errorMessage;
    private final Map<String, String> additionalParameters;
    private final String apiName;

    /**
     * Constructs a new ValidationException with the specified details.
     * 
     * @param errorCode        Unique error code for the validation failure
     * @param errorMessage     Description of the validation error
     * @param additionalParams Additional context parameters (can be null)
     * @param apiName          Name of the API where the validation failed
     */
    public ValidationException(String errorCode, String errorMessage, 
            Map<String, String> additionalParams, String apiName) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.additionalParameters = additionalParams != null 
                ? Collections.unmodifiableMap(additionalParams)
                : Collections.emptyMap();
        this.apiName = apiName;
    }

    /**
     * Gets the error message.
     * 
     * @return The error message
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Gets the error code.
     * 
     * @return The error code
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Gets additional parameters providing extra context.
     * 
     * @return Unmodifiable map of additional parameters
     */
    public Map<String, String> getAdditionalParams() {
        return this.additionalParameters;
    }

    /**
     * Gets the API name where the validation failed.
     * 
     * @return The API name
     */
    public String getApiName() {
        return this.apiName;
    }

    /**
     * Generates a structured error response as a JSONObject.
     * 
     * @param responseKey The key under which to place the error details
     * @return A JSONObject containing the error response
     */
    public JSONObject getExceptionResponse(String responseKey) {
        JSONObject response = new JSONObject();
        JSONObject errorDetails = new JSONObject();
        try {
            errorDetails.put("errorCode", this.errorCode);
            errorDetails.put("errorMessage", this.errorMessage);
            errorDetails.put("status", "validation_error");
            
            // Add additional parameters if any
            for (Map.Entry<String, String> entry : additionalParameters.entrySet()) {
                errorDetails.put(entry.getKey(), entry.getValue());
            }
            
            response.put(responseKey, errorDetails);
        } catch (JSONException e) {
            LOGGER.error("ValidationException :: Error creating exception response: {}", e.getMessage());
        }
        return response;
    }
}
