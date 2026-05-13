package org.example.gatewayexample;

import org.springframework.cloud.gateway.server.mvc.filter.FilterSupplier;
import org.springframework.cloud.gateway.server.mvc.filter.SimpleFilterSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

public class CustomFilterFunctions {

    /**
     * Gateway filter that converts 5xx backend responses into exceptions,
     * so Spring Boot's error handling (via /error) processes them.
     */
    public static HandlerFilterFunction<ServerResponse, ServerResponse> throwOnServerError() {
        return (request, next) -> {
            ServerResponse response = next.handle(request);
            if (response.statusCode().is5xxServerError()) {
                throw new ErrorResponseException(response.statusCode());
            }
            return response;
        };
    }

    @Configuration
    static class FilterSupplierConfig {

        @Bean
        public FilterSupplier customFilterSupplier() {
            return new SimpleFilterSupplier(CustomFilterFunctions.class);
        }
    }
}

