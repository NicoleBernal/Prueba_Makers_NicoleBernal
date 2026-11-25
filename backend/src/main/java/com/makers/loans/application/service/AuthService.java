package com.makers.loans.application.service;

import com.makers.loans.application.model.AuthResult;
import com.makers.loans.application.exception.InvalidCredentialsException;
import com.makers.loans.application.port.in.AuthUseCase;
import com.makers.loans.application.port.out.PasswordPort;
import com.makers.loans.application.port.out.TokenPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.domain.model.User;
import java.util.Locale;

public class AuthService implements AuthUseCase {
    private final UserRepositoryPort users;
    private final PasswordPort passwords;
    private final TokenPort tokens;

    public AuthService(UserRepositoryPort users, PasswordPort passwords, TokenPort tokens) {
        this.users = users; this.passwords = passwords; this.tokens = tokens;
    }

    @Override
    public AuthResult login(String email, String password) {
        User user = users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwords.matches(password, user.getPassword())) throw new InvalidCredentialsException();
        return new AuthResult(
                tokens.generate(user.getEmail(), user.getRole().name()),
                "Bearer",
                user.getEmail(),
                user.getName(),
                user.getRole().name());
    }
}
