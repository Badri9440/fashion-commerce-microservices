
package com.fashioncommerce.order.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void mapsKeycloakCustomerRole() {
        Jwt jwt = createJwt(List.of("CUSTOMER"));

        var authentication = config.jwtConverter().convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void mapsKeycloakAdminRole() {
        Jwt jwt = createJwt(List.of("ADMIN"));

        var authentication = config.jwtConverter().convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void missingRolesDoNotGrantAccess() {
        Jwt jwt = createJwt(List.of());

        var authentication = config.jwtConverter().convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }

    private Jwt createJwt(List<String> roles) {
        return new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "test-user",
                        "realm_access", Map.of("roles", roles)
                )
        );
    }
}
