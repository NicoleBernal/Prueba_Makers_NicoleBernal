package com.makers.loans.interfaces.rest.dto;

import com.makers.loans.application.model.AuthResult;

public class AuthResponse {

    private String token;
    private String tokenType;
    private String email;
    private String name;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tokenType, String email, String name, String role) {
        this.token = token;
        this.tokenType = tokenType;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.token(), result.tokenType(), result.email(), result.name(), result.role());
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
