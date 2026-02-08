package edu.uclm.reper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
        hibernate5Module.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);

        return Jackson2ObjectMapperBuilder.json()
                .modules(hibernate5Module)
                .build();
    }
}
