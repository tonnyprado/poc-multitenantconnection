package com.example.multitenantpoc.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    public static final String HEADER_SERVICE_ACCOUNT = "X-Service-Account";
    public static final String HEADER_TENANT_ID = "X-Tenant-ID";

    private final ServiceAccountTenantResolver resolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        try {
            String serviceAccount = request.getHeader(HEADER_SERVICE_ACCOUNT);
            String tenantIdHeader = request.getHeader(HEADER_TENANT_ID);

            String resolvedTenant = resolver.resolveTenant(serviceAccount, tenantIdHeader);

            if (resolvedTenant == null) {
                response.sendError(
                        HttpStatus.FORBIDDEN.value(),
                        "Service account no autorizado para este tenant"
                );
                return;
            }

            TenantContext.setCurrentTenant(resolvedTenant);
            log.debug("TenantContext seteado a {}", resolvedTenant);

            chain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}
