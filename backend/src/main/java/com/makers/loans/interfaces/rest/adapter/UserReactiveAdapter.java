package com.makers.loans.interfaces.rest.adapter;

import com.makers.loans.application.exception.ResourceNotFoundException;
import com.makers.loans.application.port.in.UserUseCase;
import com.makers.loans.domain.model.User;
import com.makers.loans.interfaces.rest.dto.CreateUserRequest;
import com.makers.loans.interfaces.rest.dto.UpdateUserRequest;
import com.makers.loans.interfaces.rest.dto.UserResponse;
import com.makers.loans.interfaces.rest.mapper.UserWebMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class UserReactiveAdapter {
    private final UserUseCase users;
    private final UserWebMapper mapper;

    public UserReactiveAdapter(UserUseCase users, UserWebMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    public Flux<UserResponse> findAll() {
        return Flux.defer(() -> Flux.fromIterable(users.findAll()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(mapper::toResponse);
    }

    public Mono<UserResponse> findById(Long id) {
        return Mono.fromCallable(() -> users.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(mapper::toResponse);
    }

    public Mono<UserResponse> create(CreateUserRequest request) {
        return Mono.fromCallable(() -> mapper.toResponse(users.create(mapper.toDomain(request))))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserResponse> update(Long id, UpdateUserRequest request) {
        return Mono.fromCallable(() -> mapper.toResponse(users.update(id, mapper.toDomain(request), request.getPassword())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> delete(Long id, String actingEmail) {
        return Mono.fromRunnable(() -> {
                    User admin = users.findByEmail(actingEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"));
                    users.delete(id, admin.getId());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
