package com.osttra.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;


@Configuration
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (  response.getStatusCode() == HttpStatus.NO_CONTENT) {
            // Log the 204 No Content response as successful, but don't throw an exception
            System.out.println("Received a 204 No Content response. It's treated as successful.");
        } else {
            // For other response codes, you can handle them as needed or throw exceptions
        	System.out.println("hello");
            super.handleError(response);
        }
    }
}
