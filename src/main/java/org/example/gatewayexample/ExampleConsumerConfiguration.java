package org.example.gatewayexample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ExampleConsumerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ExampleConsumerConfiguration.class);

    @Bean
    public Consumer<String> exampleConsumer() {
        return payload -> log.info("Received message: {}", payload);
    }
}

