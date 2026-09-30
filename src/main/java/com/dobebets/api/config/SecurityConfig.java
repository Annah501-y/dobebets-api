package com.dobebets.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Keep all prediction administration restricted to the tipster.
                                                .requestMatchers("/api/admin/predictions/**")
                                                .authenticated()
                                                // Require authentication for the administrative fixture sync operation.
                                                .requestMatchers(HttpMethod.POST, "/api/admin/matches/sync")
                                                .authenticated()
                                                // Require the tipster to authenticate before managing mkekas.
                                                .requestMatchers("/api/admin/bet-slips/**")
                                                .authenticated()
                                                // Keep the application's API routes publicly readable during
                                                // development.
                                                .requestMatchers("/api/**", "/error")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .httpBasic(Customizer.withDefaults())
                                .build();

        }
}
