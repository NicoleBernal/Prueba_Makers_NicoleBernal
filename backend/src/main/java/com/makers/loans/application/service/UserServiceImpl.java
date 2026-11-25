package com.makers.loans.application.service;

import com.makers.loans.domain.model.User;
import com.makers.loans.application.port.in.UserUseCase;
import com.makers.loans.application.port.out.PasswordPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.application.exception.BusinessConflictException;
import com.makers.loans.application.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordPort passwords;

    public UserServiceImpl(UserRepositoryPort userRepository, PasswordPort passwords) {
        this.userRepository = userRepository;
        this.passwords = passwords;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User create(User user) {
        String email = normalizeEmail(user.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessConflictException("Ya existe un usuario con ese correo");
        }
        user.setEmail(email);
        user.setName(normalizeName(user.getName()));
        user.setPassword(passwords.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User changes, String rawPassword) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        String email = normalizeEmail(changes.getEmail());
        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> { throw new BusinessConflictException("Ya existe un usuario con ese correo"); });
        existing.setEmail(email);
        existing.setName(normalizeName(changes.getName()));
        existing.setRole(changes.getRole());
        if (rawPassword != null && !rawPassword.isBlank()) {
            existing.setPassword(passwords.encode(rawPassword));
        }
        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id, Long actingAdminId) {
        if (id.equals(actingAdminId)) {
            throw new BusinessConflictException("No puedes eliminar tu propio usuario");
        }
        if (userRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(id);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name.trim();
    }
}
