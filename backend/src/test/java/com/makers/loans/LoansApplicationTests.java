package com.makers.loans;

import com.makers.loans.interfaces.rest.dto.AuthRequest;
import com.makers.loans.interfaces.rest.dto.LoanRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "jwt.secret=test-only-secret-key-with-at-least-thirty-two-bytes",
        "jwt.expiration-ms=3600000"
})
class LoansApplicationTests {
    @Autowired WebTestClient client;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void login() {
        userToken = token("usuario@test.com", "123");
        adminToken = token("admin@test.com", "123");
    }

    @Test
    void loginAndProtectedRoutesKeepTheirContracts() {
        client.get().uri("/api/loans/my-loans").exchange().expectStatus().isUnauthorized();
        client.get().uri("/api/loans/my-loans").headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk().expectBody().jsonPath("$").isArray();
        client.get().uri("/api/admin/loans").headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isForbidden();
    }

    @Test
    void loanLifecycleIsReactiveAndCacheIsInvalidated() {
        client.post().uri("/api/loans").headers(h -> h.setBearerAuth(userToken))
                .bodyValue(new LoanRequest(new BigDecimal("15000.00"), 36))
                .exchange().expectStatus().isCreated().expectBody()
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.userEmail").isEqualTo("usuario@test.com")
                .jsonPath("$.borrowerName").isEqualTo("Usuario Demo");
        Number createdId = client.get().uri("/api/admin/loans/pending").headers(h -> h.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk().expectBodyList(java.util.Map.class)
                .returnResult().getResponseBody().stream()
                .map(m -> (Number)m.get("id")).max(java.util.Comparator.comparingLong(Number::longValue)).orElseThrow();
        client.patch().uri("/api/admin/loans/{id}/approve", createdId.longValue())
                .headers(h -> h.setBearerAuth(adminToken)).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("APPROVED");
        client.get().uri("/api/loans/{id}", createdId.longValue()).headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.status").isEqualTo("APPROVED");
    }

    @Test
    void adminCanRejectLoanAfterConfirmationFlow() {
        Number loanId = client.post().uri("/api/loans").headers(h -> h.setBearerAuth(userToken))
                .bodyValue(new LoanRequest(new BigDecimal("12000.00"), 24))
                .exchange().expectStatus().isCreated().expectBody(java.util.Map.class)
                .returnResult().getResponseBody().get("id") instanceof Number id ? id : null;

        client.patch().uri("/api/admin/loans/{id}/reject", loanId.longValue())
                .headers(h -> h.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.status").isEqualTo("REJECTED");

        client.get().uri("/api/loans/{id}", loanId.longValue()).headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.status").isEqualTo("REJECTED");
    }

    @Test
    void adminEditsLoanAndOwnerReadsFreshValuesAfterCacheEviction() {
        Number loanId = client.post().uri("/api/loans").headers(h -> h.setBearerAuth(userToken))
                .bodyValue(new LoanRequest(new BigDecimal("3000.00"), 6))
                .exchange().expectStatus().isCreated().expectBody(java.util.Map.class)
                .returnResult().getResponseBody().get("id") instanceof Number id ? id : null;

        // Calienta ambas cachés (por ID y por usuario).
        client.get().uri("/api/loans/{id}", loanId.longValue()).headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.amount").isEqualTo(3000.0);
        client.get().uri("/api/loans/my-loans").headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk();

        client.put().uri("/api/admin/loans/{id}", loanId.longValue())
                .headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(new LoanRequest(new BigDecimal("4500.00"), 18))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.amount").isEqualTo(4500.0)
                .jsonPath("$.termMonths").isEqualTo(18)
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.updatedAt").isNotEmpty();

        client.get().uri("/api/loans/{id}", loanId.longValue()).headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.amount").isEqualTo(4500.0)
                .jsonPath("$.termMonths").isEqualTo(18);
        client.get().uri("/api/loans/my-loans").headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[?(@.id == " + loanId.longValue() + ")].amount").isEqualTo(4500.0);

        client.delete().uri("/api/admin/loans/{id}", loanId.longValue())
                .headers(h -> h.setBearerAuth(adminToken))
                .exchange().expectStatus().isNoContent();
        client.get().uri("/api/loans/{id}", loanId.longValue())
                .headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void loanEditionIsAdminOnlyValidatedAndReportsMissingLoans() {
        client.put().uri("/api/admin/loans/{id}", 1L).headers(h -> h.setBearerAuth(userToken))
                .bodyValue(new LoanRequest(new BigDecimal("500.00"), 12))
                .exchange().expectStatus().isForbidden();
        client.put().uri("/api/admin/loans/{id}", 1L).headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(new LoanRequest(new BigDecimal("50.00"), 2))
                .exchange().expectStatus().isBadRequest();
        client.put().uri("/api/admin/loans/{id}", 1L).headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(new LoanRequest(new BigDecimal("500.123"), 12))
                .exchange().expectStatus().isBadRequest();
        client.put().uri("/api/admin/loans/{id}", 987654L).headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(new LoanRequest(new BigDecimal("500.00"), 12))
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void malformedBodiesAndUnknownRoutesKeepTheirRealStatus() {
        client.post().uri("/api/loans").headers(h -> h.setBearerAuth(userToken))
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue("{ no es json")
                .exchange().expectStatus().isBadRequest();
        client.get().uri("/api/ruta-inexistente").headers(h -> h.setBearerAuth(userToken))
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void finalLoanStatusCannotBeChangedAgain() {
        Number loanId = client.post().uri("/api/loans").headers(h -> h.setBearerAuth(userToken))
                .bodyValue(new LoanRequest(new BigDecimal("6000.00"), 12))
                .exchange().expectStatus().isCreated().expectBody(java.util.Map.class)
                .returnResult().getResponseBody().get("id") instanceof Number id ? id : null;

        client.patch().uri("/api/admin/loans/{id}/approve", loanId.longValue())
                .headers(h -> h.setBearerAuth(adminToken)).exchange().expectStatus().isOk();
        client.patch().uri("/api/admin/loans/{id}/reject", loanId.longValue())
                .headers(h -> h.setBearerAuth(adminToken)).exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void invalidCredentialsAndValidationHaveClearStatuses() {
        client.post().uri("/api/auth/login").bodyValue(new AuthRequest("usuario@test.com", "bad"))
                .exchange().expectStatus().isUnauthorized();
        client.post().uri("/api/loans").headers(h -> h.setBearerAuth(userToken))
                .bodyValue(new LoanRequest(new BigDecimal("50.00"), 2))
                .exchange().expectStatus().isBadRequest();
    }

    private String token(String email, String password) {
        byte[] body = client.post().uri("/api/auth/login").bodyValue(new AuthRequest(email, password))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.role").exists()
                .returnResult().getResponseBody();
        return body == null ? failToken() : parseToken(new String(body));
    }

    private String parseToken(String json) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().readTree(json).get("token").asText(); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    private String failToken() { throw new AssertionError("Respuesta de login vacía"); }
}
