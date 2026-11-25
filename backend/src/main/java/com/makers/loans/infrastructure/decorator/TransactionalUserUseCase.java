package com.makers.loans.infrastructure.decorator;

import com.makers.loans.application.port.in.UserUseCase;
import com.makers.loans.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class TransactionalUserUseCase implements UserUseCase {
    private final UserUseCase delegate;

    public TransactionalUserUseCase(UserUseCase delegate) {
        this.delegate = delegate;
    }

    @Override public Optional<User> findByEmail(String email) { return delegate.findByEmail(email); }
    @Override public Optional<User> findById(Long id) { return delegate.findById(id); }
    @Override public List<User> findAll() { return delegate.findAll(); }
    @Override public User create(User user) { return delegate.create(user); }
    @Override public User update(Long id, User user, String rawPassword) { return delegate.update(id, user, rawPassword); }
    @Override public void delete(Long id, Long actingAdminId) { delegate.delete(id, actingAdminId); }
}
