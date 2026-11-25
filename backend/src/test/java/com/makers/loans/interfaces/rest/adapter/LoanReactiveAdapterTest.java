package com.makers.loans.interfaces.rest.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.makers.loans.application.exception.ForbiddenOperationException;
import com.makers.loans.application.exception.ResourceNotFoundException;
import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.application.port.in.UserUseCase;
import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.domain.model.User;
import com.makers.loans.interfaces.rest.mapper.LoanWebMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoanReactiveAdapterTest {

    @Mock
    private LoanUseCase loans;

    @Mock
    private UserUseCase users;

    private LoanReactiveAdapter adapter;
    private User owner;

    @BeforeEach
    void setUp() {
        adapter = new LoanReactiveAdapter(loans, users, new LoanWebMapper());
        owner = User.builder().id(7L).email("owner@test.com").name("Propietaria").build();
    }

    @Test
    void findPendingLoansEmitsMappedResultsInOrder() {
        when(loans.findAllPending()).thenReturn(List.of(
                loan(1L, LoanStatus.PENDING, owner),
                loan(2L, LoanStatus.PENDING, owner)));

        StepVerifier.create(adapter.findPendingLoans())
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                    assertThat(response.getUserEmail()).isEqualTo("owner@test.com");
                    assertThat(response.getBorrowerName()).isEqualTo("Propietaria");
                    assertThat(response.getStatus()).isEqualTo("PENDING");
                })
                .assertNext(response -> assertThat(response.getId()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    void findLoansByCurrentUserPropagatesMissingUserAsReactiveError() {
        when(users.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        StepVerifier.create(adapter.findLoansByCurrentUser("missing@test.com"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResourceNotFoundException.class);
                    assertThat(error).hasMessage("Usuario no encontrado");
                })
                .verify();
    }

    @Test
    void findLoanByIdRejectsAccessToAnotherUsersLoan() {
        when(loans.findById(10L)).thenReturn(Optional.of(loan(10L, LoanStatus.PENDING, owner)));

        StepVerifier.create(adapter.findLoanById(10L, "intruder@test.com"))
                .expectError(ForbiddenOperationException.class)
                .verify();
    }

    @Test
    void findLoanByIdAllowsOwnerToReadOwnLoan() {
        when(loans.findById(10L)).thenReturn(Optional.of(loan(10L, LoanStatus.APPROVED, owner)));

        StepVerifier.create(adapter.findLoanById(10L, "owner@test.com"))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(10L);
                    assertThat(response.getStatus()).isEqualTo("APPROVED");
                })
                .verifyComplete();
    }

    @Test
    void createBuildsLoanForAuthenticatedUserAndMapsResult() {
        BigDecimal amount = new BigDecimal("9000.00");
        when(users.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(loans.createLoan(org.mockito.ArgumentMatchers.any(Loan.class)))
                .thenAnswer(invocation -> {
                    Loan created = invocation.getArgument(0);
                    created.setId(20L);
                    return created;
                });

        StepVerifier.create(adapter.create("owner@test.com", amount, 24))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(20L);
                    assertThat(response.getAmount()).isEqualByComparingTo(amount);
                    assertThat(response.getTermMonths()).isEqualTo(24);
                    assertThat(response.getUserEmail()).isEqualTo("owner@test.com");
                    assertThat(response.getBorrowerName()).isEqualTo("Propietaria");
                })
                .verifyComplete();
    }

    @Test
    void approvePropagatesUseCaseFailureThroughMono() {
        when(users.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(User.builder().id(1L).email("admin@test.com").build()));
        when(loans.approveLoan(30L, 1L)).thenThrow(new IllegalStateException("concurrent update"));

        StepVerifier.create(adapter.approve(30L, "admin@test.com"))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && error.getMessage().equals("concurrent update"))
                .verify();
    }

    private Loan loan(Long id, LoanStatus status, User user) {
        return Loan.builder()
                .id(id)
                .amount(new BigDecimal("1200.00"))
                .termMonths(12)
                .status(status)
                .user(user)
                .build();
    }
}
