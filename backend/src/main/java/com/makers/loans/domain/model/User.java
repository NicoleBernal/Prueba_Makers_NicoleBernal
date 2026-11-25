package com.makers.loans.domain.model;

import java.time.LocalDateTime;

/** Modelo de dominio sin dependencias de frameworks. */
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private UserRole role;
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String email, String password, String name, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static final class Builder {
        private Long id;
        private String email;
        private String password;
        private String name;
        private UserRole role;
        private LocalDateTime createdAt;
        public Builder id(Long value) { id = value; return this; }
        public Builder email(String value) { email = value; return this; }
        public Builder password(String value) { password = value; return this; }
        public Builder name(String value) { name = value; return this; }
        public Builder role(UserRole value) { role = value; return this; }
        public Builder createdAt(LocalDateTime value) { createdAt = value; return this; }
        public User build() { return new User(id, email, password, name, role, createdAt); }
    }
}
