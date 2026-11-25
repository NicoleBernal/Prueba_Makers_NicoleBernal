package com.makers.loans.infrastructure.config;

import static org.mockito.Mockito.when;

import com.makers.loans.application.port.out.TokenPort.TokenClaims;
import com.makers.loans.infrastructure.security.JwtAuthenticationFilter;
import com.makers.loans.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class SecurityConfigTest {

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        JwtTokenProvider tokens = org.mockito.Mockito.mock(JwtTokenProvider.class);
        when(tokens.parse("user-token")).thenReturn(new TokenClaims("user@test.com", "USER"));
        when(tokens.parse("admin-token")).thenReturn(new TokenClaims("admin@test.com", "ADMIN"));
        when(tokens.parse("invalid-token")).thenThrow(new IllegalArgumentException("invalid"));

        SecurityConfig config = new SecurityConfig(new JwtAuthenticationFilter(tokens));
        SecurityWebFilterChain security = config.filterChain(ServerHttpSecurity.http());
        client = WebTestClient.bindToController(new StubController())
                .webFilter(new WebFilterChainProxy(security))
                .build();
    }

    @Test
    void authEndpointIsPublic() {
        client.get().uri("/api/auth/ping")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedEndpointRejectsMissingAndInvalidTokens() {
        client.get().uri("/api/loans/my-loans")
                .exchange()
                .expectStatus().isUnauthorized();

        client.get().uri("/api/loans/my-loans")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void regularUserCannotAccessAdminEndpoint() {
        client.get().uri("/api/admin/loans")
                .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void administratorCanAccessAdminEndpoints() {
        client.get().uri("/api/admin/loans")
                .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                .exchange()
                .expectStatus().isOk();
    }

    @RestController
    static class StubController {
        @GetMapping({
                "/api/auth/ping",
                "/api/loans/my-loans",
                "/api/admin/loans"
        })
        String ok() {
            return "ok";
        }
    }
}
