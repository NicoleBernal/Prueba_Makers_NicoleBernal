package com.makers.loans.interfaces.rest.controller;

import com.makers.loans.interfaces.rest.adapter.UserReactiveAdapter;
import com.makers.loans.interfaces.rest.dto.CreateUserRequest;
import com.makers.loans.interfaces.rest.dto.UpdateUserRequest;
import com.makers.loans.interfaces.rest.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {
    private final UserReactiveAdapter users;

    public UserController(UserReactiveAdapter users) {
        this.users = users;
    }

    @GetMapping
    public Flux<UserResponse> findAll() {
        return users.findAll();
    }

    @GetMapping("/{id}")
    public Mono<UserResponse> findById(@PathVariable Long id) {
        return users.findById(id);
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return users.create(request).map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user));
    }

    @PutMapping("/{id}")
    public Mono<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return users.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id, Authentication authentication) {
        return users.delete(id, authentication.getName())
                .thenReturn(ResponseEntity.noContent().build());
    }
}
