package com.dobebets.api.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Protected endpoints for the signed-in customer.
 */
@RestController
@RequestMapping("/api/account")
@Tag(name = "Customer Account", description = "Signed-in customer account")
public class CustomerAccountController {

    private final CustomerAccountService customerAccountService;

    public CustomerAccountController(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }

    /**
     * Returns the profile associated with the authenticated phone number.
     */
    @GetMapping("/me")
    @Operation(summary = "Get the signed-in customer's profile")
    @SecurityRequirement(name = "basicAuth")
    public CustomerProfileResponse getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        CustomerAccount account = customerAccountService.findByPhoneNumber(
                userDetails.getUsername());

        return new CustomerProfileResponse(
                account.getId(),
                account.getPhoneNumber());
    }

    /**
     * Excludes password hashes from customer-facing responses.
     */
    public record CustomerProfileResponse(
            Long id,
            String phoneNumber) {}
}