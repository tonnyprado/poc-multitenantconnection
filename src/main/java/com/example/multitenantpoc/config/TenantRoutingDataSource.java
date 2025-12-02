package com.example.multitenantpoc.config;

import com.example.multitenantpoc.tenant.TenantContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getCurrentTenant();
        return tenantId; // Esto debe coincidir con las keys del mapa de targetDataSources
    }
}

