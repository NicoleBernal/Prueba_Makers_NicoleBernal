package com.makers.loans.application.port.in;

import com.makers.loans.application.model.AuthResult;

public interface AuthUseCase {
    AuthResult login(String email, String password);
}
