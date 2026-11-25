package com.makers.loans.interfaces.rest.dto;

import com.makers.loans.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank @Email @Size(max = 254)
    private String email;

    @NotBlank @Size(min = 3, max = 100)
    private String password;

    @NotBlank @Size(max = 100)
    private String name;

    @NotNull
    private UserRole role;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
