package com.aem.orchestration.core.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.orchestration.core.constants.FrameworkConstants;
import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;
import com.aem.orchestration.core.services.APIOrchestrationService;

/**
 * Generic Servlet for the API Orchestration Framework.
 * 
 * This servlet acts as the entry point for all API requests that need to be
 * processed through the orchestration framework. It:
 * - Receives POST requests with a standardized JSON format
 * - Extracts context and payload information
 * - Invokes the orchestration service
 * - Returns the formatted response
 * 
 * Expected Request Format:
 * <pre>
 * {
 *   "requestString": {
 *     "context": {
 *       "journeyID": "unique-journey-id",
 *       "journeyName": "JOURNEY_NAME",
 *       "scenario": "scenario-name",
 *       "apiName": "api-identifier"
 *     },
 *     "payload": {
 *       // ... request-specific data
 *     }
 *   }
 * }
 * </pre>
 */
@Component(
    immediate = true,
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=/orchestration/api/generic",
        "sling.servlet.methods=POST",
        "sling.servlet.extensions=json"
    }
)
public class GenericServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericServlet.class);

    @Reference
    private transient APIOrchestrationService apiOrchestrationService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.info("GenericServlet :: doPost start");

        JSONArray auditInfoArray = new JSONArray();
        JSONObject parametersJson = new JSONObject();
        String journeyName = FrameworkConstants.JOURNEY_NAME_NOT_GENERATED;
        String journeyId = FrameworkConstants.JOURNEY_ID_NOT_GENERATED;
        JSONObject finalResponseContainer = new JSONObject();

        try {
            // 1. Extract and parse the request payload
            JSONObject reqStringJson = preprocessRequest(request);

            // 2. Extract common parameters
            parametersJson = extractParameters(request, reqStringJson);
            journeyId = parametersJson.optString(FrameworkConstants.JOURNEY_ID, FrameworkConstants.JOURNEY_ID_NOT_GENERATED);
            journeyName = parametersJson.optString(FrameworkConstants.JOURNEY_NAME, FrameworkConstants.JOURNEY_NAME_NOT_GENERATED);

            LOGGER.info("GenericServlet :: Processing request for journey: {} : {}", journeyName, journeyId);

            // 3. Invoke the orchestration service
            finalResponseContainer = apiOrchestrationService.doProcess(
                request, response, reqStringJson, parametersJson, auditInfoArray, journeyName, journeyId
            );

        } catch (OrchestrationException ox) {
            LOGGER.error("GenericServlet :: OrchestrationException: {} : {} :: {}", 
                        journeyName, journeyId, ox.getErrorMessage());
            finalResponseContainer = ox.getExceptionResponse(FrameworkConstants.DATA);

        } catch (ValidationException vx) {
            LOGGER.error("GenericServlet :: ValidationException: {} : {} :: {}", 
                        journeyName, journeyId, vx.getErrorMessage());
            finalResponseContainer = vx.getExceptionResponse(FrameworkConstants.DATA);

        } catch (ParseException | NoSuchAlgorithmException | JSONException e) {
            LOGGER.error("GenericServlet :: Exception: {} : {} :: {}", 
                        journeyName, journeyId, e.getMessage());
            finalResponseContainer = createErrorResponse("GENERAL_ERROR", e.getMessage());

        } finally {
            // Send the response
            sendJsonResponse(response, finalResponseContainer);
            LOGGER.info("GenericServlet :: doPost end : {} : {}", journeyName, journeyId);
        }
    }

    /**
     * Preprocesses the incoming request to extract and validate the JSON payload.
     */
    private JSONObject preprocessRequest(SlingHttpServletRequest request) 
            throws IOException, JSONException, OrchestrationException {
        
        LOGGER.info("GenericServlet :: preprocessRequest start");

        // Read the request body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String jsonInput = sb.toString();
        if (jsonInput.isEmpty()) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_INVALID_REQUEST,
                "Request body is empty",
                ""
            );
        }

        JSONObject requestPayload = new JSONObject(jsonInput);

        // Validate request format
        JSONObject reqStringJson = validateAndExtractRequestString(requestPayload);

        LOGGER.info("GenericServlet :: preprocessRequest end");
        return reqStringJson;
    }

    /**
     * Validates the request format and extracts the requestString object.
     */
    private JSONObject validateAndExtractRequestString(JSONObject requestPayload) 
            throws JSONException, OrchestrationException {

        // Check for required structure
        if (!requestPayload.has(FrameworkConstants.REQUEST_STRING)) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_INVALID_REQUEST,
                "Missing 'requestString' in request payload",
                ""
            );
        }

        JSONObject reqString = requestPayload.getJSONObject(FrameworkConstants.REQUEST_STRING);

        // Validate context
        if (!reqString.has(FrameworkConstants.CONTEXT)) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_INVALID_REQUEST,
                "Missing 'context' in requestString",
                ""
            );
        }

        JSONObject context = reqString.getJSONObject(FrameworkConstants.CONTEXT);

        // Check required context fields
        String[] requiredFields = {
            FrameworkConstants.JOURNEY_NAME,
            FrameworkConstants.JOURNEY_ID,
            FrameworkConstants.SCENARIO,
            FrameworkConstants.API_NAME
        };

        for (String field : requiredFields) {
            if (!context.has(field)) {
                throw new OrchestrationException(
                    FrameworkConstants.ERR_INVALID_REQUEST,
                    "Missing required field in context: " + field,
                    ""
                );
            }
        }

        // Validate payload
        if (!reqString.has(FrameworkConstants.PAYLOAD)) {
            throw new OrchestrationException(
                FrameworkConstants.ERR_INVALID_REQUEST,
                "Missing 'payload' in requestString",
                ""
            );
        }

        return reqString;
    }

    /**
     * Extracts and flattens parameters from the request for use by services.
     */
    private JSONObject extractParameters(SlingHttpServletRequest request, JSONObject reqStringJson) 
            throws JSONException {

        JSONObject context = reqStringJson.getJSONObject(FrameworkConstants.CONTEXT);
        JSONObject payload = reqStringJson.getJSONObject(FrameworkConstants.PAYLOAD);

        JSONObject parametersJson = new JSONObject();

        // Add context fields
        parametersJson.put(FrameworkConstants.JOURNEY_ID, 
            context.optString(FrameworkConstants.JOURNEY_ID, FrameworkConstants.JOURNEY_ID_NOT_GENERATED));
        parametersJson.put(FrameworkConstants.JOURNEY_NAME, 
            context.optString(FrameworkConstants.JOURNEY_NAME, FrameworkConstants.JOURNEY_NAME_NOT_GENERATED));
        parametersJson.put(FrameworkConstants.API_NAME, 
            context.optString(FrameworkConstants.API_NAME, FrameworkConstants.API_NAME_NOT_PASSED));
        parametersJson.put(FrameworkConstants.SCENARIO, 
            context.optString(FrameworkConstants.SCENARIO, FrameworkConstants.SCENARIO_NOT_PASSED));

        // Add User-Agent header
        String userAgent = request.getHeader(FrameworkConstants.REQUEST_USER_AGENT);
        parametersJson.put(FrameworkConstants.USER_AGENT, 
            userAgent != null ? userAgent : FrameworkConstants.USER_AGENT_NOT_PASSED);

        // Flatten payload into parameters
        Iterator<String> keys = payload.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            parametersJson.put(key, payload.get(key));
        }

        LOGGER.info("GenericServlet :: extractParameters :: journey: {} : {}", 
            parametersJson.optString(FrameworkConstants.JOURNEY_NAME),
            parametersJson.optString(FrameworkConstants.JOURNEY_ID));

        return parametersJson;
    }

    /**
     * Creates a generic error response.
     */
    private JSONObject createErrorResponse(String errorCode, String errorMessage) {
        JSONObject response = new JSONObject();
        try {
            JSONObject error = new JSONObject();
            error.put("errorCode", errorCode);
            error.put("errorMessage", errorMessage);
            error.put("status", "error");
            response.put(FrameworkConstants.DATA, error);
        } catch (JSONException e) {
            LOGGER.error("GenericServlet :: Error creating error response: {}", e.getMessage());
        }
        return response;
    }

    /**
     * Sends a JSON response to the client.
     */
    private void sendJsonResponse(SlingHttpServletResponse response, JSONObject jsonResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResponse.toString());
            writer.flush();
        }
    }
}
