package com.makers.loans.interfaces.rest.controller;

import com.makers.loans.application.port.in.AuthUseCase;
import com.makers.loans.interfaces.rest.dto.AuthRequest;
import com.makers.loans.interfaces.rest.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase auth;

    public AuthController(AuthUseCase auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return Mono.fromCallable(() -> auth.login(request.getEmail(), request.getPassword()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(AuthResponse::from);
    }
}
