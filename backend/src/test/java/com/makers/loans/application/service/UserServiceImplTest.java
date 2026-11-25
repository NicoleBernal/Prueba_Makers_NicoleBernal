package com.makers.loans.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.makers.loans.application.exception.BusinessConflictException;
import com.makers.loans.application.port.out.PasswordPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.domain.model.User;
import com.makers.loans.domain.model.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepositoryPort users;
    @Mock private PasswordPort passwords;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(users, passwords);
    }

    @Test
    void createNormalizesAndHashesThePassword() {
        User user = User.builder().email(" New@Example.COM ").name(" New user ").password("secret").role(UserRole.USER).build();
        when(users.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwords.encode("secret")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = service.create(user);

        assertEquals("new@example.com", saved.getEmail());
        assertEquals("New user", saved.getName());
        assertEquals("hashed", saved.getPassword());
        verify(users).save(user);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(users.findByEmail("new@example.com")).thenReturn(Optional.of(User.builder().id(3L).build()));

        assertThrows(BusinessConflictException.class, () -> service.create(
                User.builder().email("new@example.com").name("New").password("secret").role(UserRole.USER).build()));
    }

    @Test
    void updatePreservesPasswordWhenItIsOmitted() {
        User existing = User.builder().id(3L).email("old@example.com").name("Old").password("hashed").role(UserRole.USER).build();
        when(users.findById(3L)).thenReturn(Optional.of(existing));
        when(users.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(users.save(existing)).thenReturn(existing);

        User updated = service.update(3L, User.builder().email("new@example.com").name("New").role(UserRole.ADMIN).build(), null);

        assertEquals("hashed", updated.getPassword());
        assertEquals(UserRole.ADMIN, updated.getRole());
    }

    @Test
    void cannotDeleteTheAuthenticatedAdministrator() {
        assertThrows(BusinessConflictException.class, () -> service.delete(3L, 3L));
    }
}
