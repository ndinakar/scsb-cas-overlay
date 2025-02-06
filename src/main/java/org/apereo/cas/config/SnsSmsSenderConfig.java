package org.apereo.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@ComponentScan(basePackages = "org.apereo.cas.config")
public class SnsSmsSenderConfig {

    @Bean
    @DependsOn("communicationsManager")
    public SnsSmsSender snsSmsSender() {
        return new SnsSmsSender();
    }
}