package com.example.multitenantpoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "tenants")
public class TenantDataSourceProperties {
    private Map<String, SimpleDataSourceProperties> datasources;

    @Data
    public static class SimpleDataSourceProperties {
        private String url;
        private String username;
        private String password;
    }
}
