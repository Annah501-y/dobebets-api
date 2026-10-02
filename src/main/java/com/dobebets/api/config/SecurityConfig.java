package com.dobebets.api.config;

import com.dobebets.api.account.CustomerAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures authentication for the tipster and customer accounts.
 */
@Configuration
public class SecurityConfig {

        /**
         * Protects admin routes so only the tipster role can manage platform data.
         */
        @Bean
        SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers("/api/auth/**")
                                                .permitAll()
                                                // Customer profile routes require a customer account.
                                                .requestMatchers("/api/account/**")
                                                .hasRole("CUSTOMER")
                                                // Only signed-in customers may submit payment references.
                                                .requestMatchers("/api/payments/**")
                                                .hasRole("CUSTOMER")
                                                .requestMatchers("/api/admin/predictions/**")
                                                .hasRole("TIPSTER")
                                                .requestMatchers(HttpMethod.POST, "/api/admin/matches/sync")
                                                .hasRole("TIPSTER")
                                                .requestMatchers("/api/admin/bet-slips/**")
                                                .hasRole("TIPSTER")
                                                // Restrict payment review actions to the tipster.
                                                .requestMatchers("/api/admin/payments/**")
                                                .hasRole("TIPSTER")
                                                // Require a signed-in customer to start a mkeka purchase.
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/bet-slips/*/purchase")
                                                .hasRole("CUSTOMER")
                                                // Only authenticated customers can request mkeka selections.
                                                .requestMatchers(
                                                        HttpMethod.GET,
                                                        "/api/bet-slips/*/selections")
                                                .hasRole("CUSTOMER")        
                                                .requestMatchers("/api/**", "/error")
                                                .permitAll()
                                                .anyRequest()
                                                .authenticated())
                                .httpBasic(Customizer.withDefaults())
                                .build();
        }

        /**
         * Uses BCrypt to store customer passwords as one-way hashes.
         */
        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Loads the tipster from configuration and customers from PostgreSQL.
         */
        @Bean
        UserDetailsService userDetailsService(
                        CustomerAccountRepository customerAccountRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${spring.security.user.name}") String tipsterUsername,
                        @Value("${spring.security.user.password}") String tipsterPassword) {

                UserDetails tipster = User.withUsername(tipsterUsername)
                                .password(passwordEncoder.encode(tipsterPassword))
                                .roles("TIPSTER")
                                .build();

                return username -> {
                        if (tipsterUsername.equals(username)) {
                                return tipster;
                        }

                        return customerAccountRepository.findByPhoneNumber(username)
                                        .map(customer -> User.withUsername(customer.getPhoneNumber())
                                                        .password(customer.getPasswordHash())
                                                        .roles(customer.getRole().name())
                                                        .build())
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "No account found for the supplied phone number."));
                };
        }
}