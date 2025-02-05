package org.apereo.cas.config;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import javax.sql.DataSource;

@Configuration(value = "CasOverlayOverrideConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOverlayOverrideConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasOverlayOverrideConfiguration.class);
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.subject}")
    private String mailSubject;

    @Value("${spring.mail.sqlQuery}")
    private String sqlQuery;


    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CommunicationsManager communicationsManager(JavaMailSender javaMailSender, @Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new CustomCustomCommunicationsManager(javaMailSender,jdbcTemplate,from,mailSubject,sqlQuery);
    }

    @Bean
    public DataSource myDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


}
