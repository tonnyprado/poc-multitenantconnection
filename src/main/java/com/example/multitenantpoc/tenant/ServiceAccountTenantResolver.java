package com.example.multitenantpoc.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ServiceAccountTenantResolver {

    private final Map<String, String> serviceAccountToTenant = Map.of(
            "sa_empresa_a", "tenant_a",
            "sa_empresa_b", "tenant_b"
    );

    public String resolveTenant(String serviceAccountHeader, String requestedTenantId) {

        if (serviceAccountHeader == null || requestedTenantId == null) {
            log.warn("Faltan headers obligatorios: X-Service-Account o X-Tenant-ID");
            return null;
        }

        String allowedTenant = serviceAccountToTenant.get(serviceAccountHeader);

        if (allowedTenant == null) {
            log.warn("Service account no reconocido: {}", serviceAccountHeader);
            return null;
        }

        if (!allowedTenant.equals(requestedTenantId)) {
            log.warn("Service account {} no tiene acceso al tenant {} (solo tiene acceso a {}).",
                    serviceAccountHeader, requestedTenantId, allowedTenant);
            return null;
        }

        log.debug("Service account {} autorizado para tenant {}", serviceAccountHeader, allowedTenant);
        return allowedTenant;
    }
}
