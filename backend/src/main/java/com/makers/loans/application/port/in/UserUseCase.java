package com.makers.loans.application.port.in;

import com.makers.loans.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserUseCase {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAll();
    User create(User user);
    User update(Long id, User user, String rawPassword);
    void delete(Long id, Long actingAdminId);
}
