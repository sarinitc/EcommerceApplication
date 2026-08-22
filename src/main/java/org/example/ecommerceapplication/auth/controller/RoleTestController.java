package org.example.ecommerceapplication.auth.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
public class RoleTestController {

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customer() {
        return "Hello CUSTOMER";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "Hello ADMIN";
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public String seller() {
        return "Hello SELLER";
    }
}