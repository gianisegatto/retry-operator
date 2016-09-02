package com.example.configuration;

import com.example.RetryOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfiguration {

    @Value("${retry.attempts}")
    private int retryAttempts;

    @Value("${retry.delay}")
    private int retryDelay;

    @Bean
    public RetryOperator<String, String> retryOperator() {
        return new RetryOperator<>(retryAttempts, retryDelay, RuntimeException.class);
    }
}
