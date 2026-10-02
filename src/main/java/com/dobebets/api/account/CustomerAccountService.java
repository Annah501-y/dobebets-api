package com.dobebets.api.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies account registration rules and securely hashes customer passwords.
 */
@Service
public class CustomerAccountService {

    private final CustomerAccountRepository customerAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAccountService(
            CustomerAccountRepository customerAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.customerAccountRepository = customerAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a customer account using an international-format phone number.
     */
    @Transactional
    public CustomerAccount register(String phoneNumber, String rawPassword) {
        String normalizedPhoneNumber =
                normalizeTanzaniaPhoneNumber(phoneNumber);

        if (rawPassword == null
                || rawPassword.length() < 8
                || rawPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "Password must be between 8 and 72 characters.");
        }

        if (customerAccountRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new IllegalStateException(
                    "An account already exists for that phone number.");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        return customerAccountRepository.save(
                new CustomerAccount(normalizedPhoneNumber, passwordHash));
    }

        /**
     * Normalizes accepted Tanzanian mobile formats to the +255 international form.
     */
        private String normalizeTanzaniaPhoneNumber(String phoneNumber) {
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("Phone number is required.");
            }
    
            String digits = phoneNumber.trim().replaceAll("[\\s()-]", "");
    
            if (digits.startsWith("+255")) {
                digits = digits.substring(4);
            } else if (digits.startsWith("255")) {
                digits = digits.substring(3);
            }
    
            // Remove Tanzania's local trunk prefix when the customer enters 06... or 07...
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
    
            if (!digits.matches("[67]\\d{8}")) {
                throw new IllegalArgumentException(
                        "Enter a valid Tanzanian mobile number, such as 0712345678.");
            }
    
            return "+255" + digits;
        }
            /**
     * Finds the profile for an authenticated customer.
     */
    @Transactional(readOnly = true)
    public CustomerAccount findByPhoneNumber(String phoneNumber) {
        return customerAccountRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No customer account exists for that phone number."));
    }
}