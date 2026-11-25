package com.makers.loans.interfaces.rest.mapper;

import com.makers.loans.domain.model.User;
import com.makers.loans.interfaces.rest.dto.CreateUserRequest;
import com.makers.loans.interfaces.rest.dto.UpdateUserRequest;
import com.makers.loans.interfaces.rest.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {
    public User toDomain(CreateUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .role(request.getRole())
                .build();
    }

    public User toDomain(UpdateUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .role(request.getRole())
                .build();
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getCreatedAt());
    }
}
