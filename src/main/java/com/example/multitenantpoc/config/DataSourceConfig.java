package com.example.multitenantpoc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(TenantDataSourceProperties.class)
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${tenants.driver}")
    private String driverName;

    @Bean
    public DataSource dataSource(TenantDataSourceProperties tenantDataSourceProperties) {

        Map<Object, Object> targetDataSources = new HashMap<>();

        tenantDataSourceProperties.getDatasources().forEach((tenantId, props) -> {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setUrl(props.getUrl());
            ds.setUsername(props.getUsername());
            ds.setPassword(props.getPassword());

            ds.setDriverClassName(driverName);

            log.info("Configurando DataSource para tenant {} -> {}", tenantId, props.getUrl());
            targetDataSources.put(tenantId, ds);
        });

        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);

        // Default (por si acaso no hay tenant, aunque en teoría siempre debe haber)
        routingDataSource.setDefaultTargetDataSource(targetDataSources.values().iterator().next());

        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}

