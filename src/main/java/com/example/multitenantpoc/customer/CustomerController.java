package com.example.multitenantpoc.customer;

import com.example.multitenantpoc.customer.Customer;
import com.example.multitenantpoc.customer.CustomerRequest;
import com.example.multitenantpoc.customer.CustomerService;
import com.example.multitenantpoc.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CustomerController {

    private final CustomerService service;

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getCustomers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/tenants/me")
    public ResponseEntity<Map<String, Object>> getCurrentTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(Map.of("tenantId", tenantId));
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(service.create(
                new Customer(request.getName(), request.getEmail())
        ));
    }
}
