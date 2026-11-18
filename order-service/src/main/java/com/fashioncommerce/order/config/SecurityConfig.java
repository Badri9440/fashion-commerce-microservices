package com.fashioncommerce.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/orders/warehouse/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE")
                        .requestMatchers("/api/orders/customer/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/orders/*/fulfillment")
                        .hasAnyRole("ADMIN", "WAREHOUSE")
                        .requestMatchers("/api/orders/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter())))
                .build();
    }

    JwtAuthenticationConverter jwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

            if (realmAccess == null ||
                    !(realmAccess.get("roles") instanceof Collection<?> roles)) {
                return java.util.List.of();
            }

            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(role -> (org.springframework.security.core.GrantedAuthority)
                            new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });

        return converter;
    }
}