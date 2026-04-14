package com.aem.orchestration.core.services.apiAdapterService.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.regex.Matcher;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.orchestration.core.constants.FrameworkConstants;
import com.aem.orchestration.core.exceptions.OrchestrationException;
import com.aem.orchestration.core.exceptions.ValidationException;
import com.aem.orchestration.core.services.apiAdapterService.APIAdapterService;

/**
 * Default implementation of APIAdapterService.
 * 
 * This adapter formats API responses according to a schema definition.
 * The schema maps output field names to paths in the API response.
 * 
 * Supported path formats:
 * - Simple: "fieldName" -> gets value of "fieldName" from response
 * - Nested: "parent.child.field" -> traverses nested objects
 * - Array wildcard: "items[*].name" -> extracts all "name" values from array
 * - Array index: "items[0].name" -> extracts specific index
 */
@Component(
    service = APIAdapterService.class,
    property = {"name=default-adapter-service"}
)
public class DefaultAPIAdapterServiceImpl implements APIAdapterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAPIAdapterServiceImpl.class);

    @Override
    public JSONObject format(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            JSONObject apiResponse,
            JSONObject schemaJSONObject,
            String journeyName,
            String journeyId)
            throws IOException, ValidationException, JSONException, NoSuchAlgorithmException,
                   ParseException, OrchestrationException {

        LOGGER.info("DefaultAPIAdapterServiceImpl :: format start :: {} : {}", journeyName, journeyId);

        JSONObject formattedResponse = new JSONObject();

        if (schemaJSONObject == null || schemaJSONObject.length() == 0) {
            LOGGER.warn("DefaultAPIAdapterServiceImpl :: Schema is empty, returning raw response");
            return apiResponse;
        }

        Iterator<String> keys = schemaJSONObject.keys();
        while (keys.hasNext()) {
            String outputKey = keys.next();
            String sourcePath = schemaJSONObject.getString(outputKey);
            
            try {
                Object value = extractValueFromPath(apiResponse, sourcePath);
                if (value != null) {
                    formattedResponse.put(outputKey, value);
                }
            } catch (Exception e) {
                LOGGER.debug("DefaultAPIAdapterServiceImpl :: Could not extract value for key: {} from path: {}", 
                            outputKey, sourcePath);
                // Continue processing other fields even if one fails
            }
        }

        LOGGER.info("DefaultAPIAdapterServiceImpl :: format end :: {} : {}", journeyName, journeyId);
        return formattedResponse;
    }

    /**
     * Extracts a value from a JSON object using a dot-notation path.
     * Supports nested objects and arrays with wildcard or index notation.
     * 
     * @param json   The source JSON object
     * @param path   The path to extract (e.g., "customer.address.city" or "accounts[*].balance")
     * @return The extracted value, or null if not found
     */
    private Object extractValueFromPath(JSONObject json, String path) throws JSONException {
        if (json == null || path == null || path.isEmpty()) {
            return null;
        }

        // Check if path contains array notation
        Matcher arrayMatcher = FrameworkConstants.ARRAY_PRESENT_PATTERN.matcher(path);
        if (arrayMatcher.find()) {
            return extractArrayValue(json, path);
        }

        // Simple dot-notation path
        String[] parts = path.split("\\.");
        Object current = json;

        for (String part : parts) {
            if (current instanceof JSONObject) {
                JSONObject currentObj = (JSONObject) current;
                if (currentObj.has(part)) {
                    current = currentObj.get(part);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * Extracts values from arrays using wildcard or index notation.
     * 
     * Examples:
     * - "accounts[*].balance" extracts all balance values from accounts array
     * - "accounts[0].balance" extracts balance from first account
     */
    private Object extractArrayValue(JSONObject json, String path) throws JSONException {
        // Split path at array notation
        int bracketStart = path.indexOf('[');
        int bracketEnd = path.indexOf(']');
        
        String beforeArray = path.substring(0, bracketStart);
        String arrayIndex = path.substring(bracketStart + 1, bracketEnd);
        String afterArray = bracketEnd + 1 < path.length() ? path.substring(bracketEnd + 2) : "";

        // Navigate to the array
        Object arrayObj = extractValueFromPath(json, beforeArray);
        if (!(arrayObj instanceof JSONArray)) {
            // Try direct extraction if not an array
            return extractValueFromPath(json, beforeArray);
        }

        JSONArray array = (JSONArray) arrayObj;

        if ("*".equals(arrayIndex)) {
            // Wildcard: extract from all elements
            JSONArray result = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                Object element = array.get(i);
                if (element instanceof JSONObject && !afterArray.isEmpty()) {
                    Object value = extractValueFromPath((JSONObject) element, afterArray);
                    if (value != null) {
                        result.put(value);
                    }
                } else {
                    result.put(element);
                }
            }
            return result;
        } else {
            // Specific index
            try {
                int index = Integer.parseInt(arrayIndex);
                if (index >= 0 && index < array.length()) {
                    Object element = array.get(index);
                    if (element instanceof JSONObject && !afterArray.isEmpty()) {
                        return extractValueFromPath((JSONObject) element, afterArray);
                    }
                    return element;
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("DefaultAPIAdapterServiceImpl :: Invalid array index: {}", arrayIndex);
            }
        }

        return null;
    }
}
