package org.apereo.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.apereo.cas.config")
public class SnsSmsSenderConfig {

    @Bean
    public SnsSmsSender snsSmsSender() {
        return new SnsSmsSender();
    }
}