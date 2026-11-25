package com.makers.loans.application.service;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.domain.model.User;
import com.makers.loans.domain.model.UserRole;
import com.makers.loans.application.port.out.LoanRepositoryPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.application.exception.BusinessConflictException;
import com.makers.loans.application.exception.ForbiddenOperationException;
import com.makers.loans.application.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepositoryPort loanRepository;
    @Mock
    private UserRepositoryPort userRepository;

    private LoanServiceImpl service;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new LoanServiceImpl(loanRepository, userRepository);
        admin = User.builder().id(1L).role(UserRole.ADMIN).build();
    }

    @Test
    void approveLoanSetsStatusAndReviewer() {
        Loan pending = Loan.builder().id(10L).status(LoanStatus.PENDING).build();
        when(loanRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(loanRepository.save(pending)).thenReturn(pending);

        Loan result = service.approveLoan(10L, 1L);

        assertEquals(LoanStatus.APPROVED, result.getStatus());
        assertSame(admin, result.getReviewedBy());
        verify(loanRepository).save(pending);
    }

    @Test
    void cannotApproveAlreadyReviewedLoan() {
        Loan approved = Loan.builder().id(10L).status(LoanStatus.APPROVED).build();
        when(loanRepository.findById(10L)).thenReturn(Optional.of(approved));

        assertThrows(BusinessConflictException.class, () -> service.approveLoan(10L, 1L));
        verify(loanRepository, never()).save(approved);
    }

    @Test
    void rejectLoanSetsRejectedStatusAndReviewer() {
        Loan pending = Loan.builder().id(10L).status(LoanStatus.PENDING).build();
        when(loanRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(loanRepository.save(pending)).thenReturn(pending);

        Loan result = service.rejectLoan(10L, 1L);

        assertEquals(LoanStatus.REJECTED, result.getStatus());
        assertSame(admin, result.getReviewedBy());
        verify(loanRepository).save(pending);
    }

    @Test
    void cannotRejectAlreadyApprovedLoan() {
        Loan approved = Loan.builder().id(11L).status(LoanStatus.APPROVED).build();
        when(loanRepository.findById(11L)).thenReturn(Optional.of(approved));

        assertThrows(BusinessConflictException.class, () -> service.rejectLoan(11L, 1L));
        verify(loanRepository, never()).save(approved);
    }

    @Test
    void updateLoanChangesAmountAndTermButKeepsStatus() {
        Loan approved = Loan.builder().id(13L).amount(new BigDecimal("1000.00")).termMonths(6)
                .status(LoanStatus.APPROVED).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(loanRepository.findById(13L)).thenReturn(Optional.of(approved));
        when(loanRepository.save(approved)).thenReturn(approved);

        Loan result = service.updateLoan(13L, 1L, new BigDecimal("2500.50"), 24);

        assertEquals(new BigDecimal("2500.50"), result.getAmount());
        assertEquals(24, result.getTermMonths());
        assertEquals(LoanStatus.APPROVED, result.getStatus());
        verify(loanRepository).save(approved);
    }

    @Test
    void updateLoanRequiresAdministratorRole() {
        User regular = User.builder().id(2L).role(UserRole.USER).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(regular));

        assertThrows(ForbiddenOperationException.class,
                () -> service.updateLoan(13L, 2L, new BigDecimal("500.00"), 12));
        verify(loanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateLoanFailsWhenLoanDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateLoan(99L, 1L, new BigDecimal("500.00"), 12));
        verify(loanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteLoanChecksExistenceBeforeDeleting() {
        Loan existing = Loan.builder().id(12L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(loanRepository.findById(12L)).thenReturn(Optional.of(existing));

        service.deleteLoan(12L, 1L);

        verify(loanRepository).deleteById(12L);
    }

}
