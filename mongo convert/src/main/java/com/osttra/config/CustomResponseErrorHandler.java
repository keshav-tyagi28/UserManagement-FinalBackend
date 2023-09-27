package com.osttra.config;


import org.springframework.context.annotation.Configuration;
import java.util.logging.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;


@Configuration
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {
	
    private static final Logger LOGGER = Logger.getLogger(CustomResponseErrorHandler.class.getName());

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (  response.getStatusCode() == HttpStatus.NO_CONTENT) {
            // Log the 204 No Content response as successful, but don't throw an exception
            LOGGER.info("Received a 204 No Content response. It's treated as successful.");
        } else {
            // For other response codes, you can handle them as needed or throw exceptions
            LOGGER.warning("Received an error response with status code: " + response.getStatusCode());
            super.handleError(response);
        }
    }
}
