package com.yourownai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides JacksonConfig behavior.
 */
@Configuration
public class JacksonConfig {
    /**
     * Executes objectMapper.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
