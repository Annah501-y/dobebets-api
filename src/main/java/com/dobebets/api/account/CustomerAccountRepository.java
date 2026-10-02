package com.dobebets.api.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Database lookups for customer accounts.
 */
public interface CustomerAccountRepository
        extends JpaRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}