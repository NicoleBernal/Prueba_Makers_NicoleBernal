package com.makers.loans.application.port.out;

public interface TokenPort {
    String generate(String email, String role);
    TokenClaims parse(String token);

    record TokenClaims(String email, String role) {}
}
