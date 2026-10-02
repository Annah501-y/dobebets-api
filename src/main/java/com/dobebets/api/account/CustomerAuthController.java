package com.dobebets.api.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public account registration endpoints for customers.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Customer Accounts", description = "Customer registration and authentication")
public class CustomerAuthController {

    private final CustomerAccountService customerAccountService;

    public CustomerAuthController(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }

    /**
     * Registers a customer without returning or exposing the password hash.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a customer account")
    public CustomerAccountResponse register(
            @Valid @RequestBody RegisterCustomerRequest request) {

        CustomerAccount account = customerAccountService.register(
                request.phoneNumber(),
                request.password());

        return new CustomerAccountResponse(
                account.getId(),
                account.getPhoneNumber());
    }

    /**
     * Validates the international phone number and minimum password length.
     */
    public record RegisterCustomerRequest(
            @NotBlank
            @Size(max = 20) String phoneNumber,
            @NotBlank
            @Size(min = 8, max = 72)
            String password) {}

    /**
     * Safe response fields for a newly registered account.
     */
    public record CustomerAccountResponse(
            Long id,
            String phoneNumber) {}
}