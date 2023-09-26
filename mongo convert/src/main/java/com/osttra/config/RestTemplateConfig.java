package com.osttra.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	
	@Autowired
	CustomResponseErrorHandler customResponseErrorHandler;
    @Bean
    public RestTemplate restTemplate(CustomResponseErrorHandler customResponseErrorHandler) {
        RestTemplate restTemplate = new RestTemplate();
        
        System.out.println("checking");
        // Set the custom error handler for this RestTemplate
        restTemplate.setErrorHandler(customResponseErrorHandler);

        return restTemplate;
    }
}
