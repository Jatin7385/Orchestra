package com.aem.orchestration.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.orchestration.core.services.APIOrchestrationConfig;
import com.aem.orchestration.core.services.APIOrchestrationConfigService;

/**
 * Implementation of APIOrchestrationConfigService that loads and parses
 * API orchestration configurations from OSGi config.
 */
@Component(service = APIOrchestrationConfigService.class, immediate = true)
@Designate(ocd = APIOrchestrationConfig.class)
public class APIOrchestrationConfigServiceImpl implements APIOrchestrationConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIOrchestrationConfigServiceImpl.class);

    private String[] apisToBeOrchestrated;
    private String[] validationServices;
    private String[] adapterServices;
    private String[] apiResponseSchema;

    @Activate
    @Modified
    protected final void activate(APIOrchestrationConfig config) {
        LOGGER.info("APIOrchestrationConfigServiceImpl :: Activating configuration");
        if (config != null) {
            apisToBeOrchestrated = config.getAPIOrchestrationConfig();
            validationServices = config.getValidationServiceConfig();
            adapterServices = config.getAPIAdapterServiceConfig();
            apiResponseSchema = config.getAPIResponseSchema();
            LOGGER.info("APIOrchestrationConfigServiceImpl :: Loaded {} API configs, {} validation configs, {} adapter configs",
                    apisToBeOrchestrated != null ? apisToBeOrchestrated.length : 0,
                    validationServices != null ? validationServices.length : 0,
                    adapterServices != null ? adapterServices.length : 0);
        }
    }

    /**
     * Parses the API orchestration configuration.
     * Input format: "JOURNEY_NAME|SCENARIO=API_1|API_2|API_3"
     * Output: Map with key="JOURNEY_NAME|SCENARIO", value=["API_1", "API_2", "API_3"]
     */
    @Override
    public Map<String, String[]> getAPIOrchestrationConfig() {
        Map<String, String[]> map = new HashMap<>();
        
        if (apisToBeOrchestrated != null) {
            for (String config : apisToBeOrchestrated) {
                String[] parts = config.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String[] values = parts[1].trim().split("\\|");
                    map.put(key, values);
                }
            }
        }
        
        return map;
    }

    /**
     * Parses the validation service configuration.
     * Input format: "JOURNEY_NAME|SCENARIO|API_SERVICE=VALIDATION_SERVICE"
     * Output: Map with key="JOURNEY_NAME|SCENARIO|API_SERVICE", value="VALIDATION_SERVICE"
     */
    @Override
    public Map<String, String> getValidationServiceConfig() {
        Map<String, String> map = new HashMap<>();
        
        if (validationServices != null) {
            for (String config : validationServices) {
                String[] parts = config.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    map.put(key, value);
                }
            }
        }
        
        return map;
    }

    /**
     * Parses the adapter service configuration.
     * Input format: "JOURNEY_NAME|SCENARIO=ADAPTER_SERVICE"
     * Output: Map with key="JOURNEY_NAME|SCENARIO", value="ADAPTER_SERVICE"
     */
    @Override
    public Map<String, String> getAPIAdapterServiceConfig() {
        Map<String, String> map = new HashMap<>();
        
        if (adapterServices != null) {
            for (String config : adapterServices) {
                String[] parts = config.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    map.put(key, value);
                }
            }
        }
        
        return map;
    }

    /**
     * Parses the API response schema configuration.
     * Input format: "JOURNEY_NAME={JSON_SCHEMA}"
     * Output: Map with key="JOURNEY_NAME", value="{JSON_SCHEMA}"
     */
    @Override
    public Map<String, String> getAPIResponseSchema() {
        Map<String, String> map = new HashMap<>();
        
        if (apiResponseSchema != null) {
            for (String config : apiResponseSchema) {
                String[] parts = config.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    map.put(key, value);
                }
            }
        }
        
        return map;
    }
}
