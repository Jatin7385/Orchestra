package com.aem.orchestration.core.exceptions;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrchestrationException is a custom exception class for service-related errors
 * in the API Orchestration Framework.
 * 
 * This exception captures:
 * - errorCode: unique identifier for the error type
 * - errorMessage: description of the issue
 * - apiName: the API that triggered the exception
 * 
 * It also provides a method to generate a structured JSON error response.
 */
public class OrchestrationException extends Exception implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationException.class);
    
    private final String errorCode;
    private final String errorMessage;
    private final String apiName;

    /**
     * Constructs a new OrchestrationException with the specified details.
     * 
     * @param errorCode    Unique error code identifying the error type
     * @param errorMessage Description of the error
     * @param apiName      Name of the API where the error occurred
     */
    public OrchestrationException(String errorCode, String errorMessage, String apiName) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
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
     * Gets the API name where the error occurred.
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
        JSONObject finalResponseContainer = new JSONObject();
        try {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("errorCode", this.errorCode);
            errorResponse.put("errorMessage", this.errorMessage);
            errorResponse.put("status", "error");
            finalResponseContainer.put(responseKey, errorResponse);
        } catch (JSONException e) {
            LOGGER.error("OrchestrationException :: Error creating exception response: {}", e.getMessage());
        }
        return finalResponseContainer;
    }
}
