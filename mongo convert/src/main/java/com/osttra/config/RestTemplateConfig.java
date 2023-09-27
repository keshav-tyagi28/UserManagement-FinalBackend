package com.osttra.config;


import org.springframework.beans.factory.annotation.Autowired;
import java.util.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	
    private static final Logger LOGGER = Logger.getLogger(CustomResponseErrorHandler.class.getName());

	@Autowired
	CustomResponseErrorHandler customResponseErrorHandler;
    @Bean
    public RestTemplate restTemplate(CustomResponseErrorHandler customResponseErrorHandler) {
        RestTemplate restTemplate = new RestTemplate();
        
        LOGGER.info("Creating RestTemplate bean");
        // Set the custom error handler for this RestTemplate
        restTemplate.setErrorHandler(customResponseErrorHandler);

        return restTemplate;
    }
}
