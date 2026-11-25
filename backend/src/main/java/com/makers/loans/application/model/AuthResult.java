package com.makers.loans.application.model;

public record AuthResult(
        String token,
        String tokenType,
        String email,
        String name,
        String role) {
}
