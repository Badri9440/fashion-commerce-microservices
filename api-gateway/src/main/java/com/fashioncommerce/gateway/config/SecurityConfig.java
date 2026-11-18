
package com.fashioncommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/orders/warehouse/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE")
                        .pathMatchers("/api/orders/*/fulfillment")
                        .hasAnyRole("ADMIN", "WAREHOUSE")
                        .pathMatchers("/api/orders", "/api/orders/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .pathMatchers(
                                "/api/inventory", "/api/inventory/**",
                                "/api/warehouses", "/api/warehouses/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE")

                        .pathMatchers("/api/reservations", "/api/reservations/**")
                        .denyAll()


                        .pathMatchers(HttpMethod.GET,
                                "/api/products", "/api/products/**",
                                "/api/variants", "/api/variants/**")
                        .authenticated()
                        .pathMatchers("/api/products", "/api/products/**",
                                "/api/variants", "/api/variants/**")
                        .hasRole("ADMIN")


                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {

                            Map<String, Object> realm =
                                    token.getClaimAsMap("realm_access");

                            List<SimpleGrantedAuthority> authorities =
                                    realm != null && realm.get("roles") instanceof List<?> roles
                                            ? roles.stream()
                                            .filter(String.class::isInstance)
                                            .map(String.class::cast)
                                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                            .toList()
                                            : List.of();

                            return Mono.just(
                                    new JwtAuthenticationToken(token, authorities)
                            );
                        }))
                )
                .build();
    }
}
