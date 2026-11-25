package com.makers.loans.application.service;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.domain.model.User;
import com.makers.loans.domain.model.UserRole;
import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.application.port.out.LoanRepositoryPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.application.exception.BusinessConflictException;
import com.makers.loans.application.exception.ResourceNotFoundException;
import com.makers.loans.application.exception.ForbiddenOperationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class LoanServiceImpl implements LoanUseCase {

    private final LoanRepositoryPort loanRepository;
    private final UserRepositoryPort userRepository;

    public LoanServiceImpl(LoanRepositoryPort loanRepository, UserRepositoryPort userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Loan createLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    @Override
    public Loan updateLoan(Long loanId, Long adminId, BigDecimal amount, Integer termMonths) {
        requireAdmin(findUser(adminId, "Administrador no encontrado"));
        Loan loan = findLoan(loanId);
        loan.setAmount(amount);
        loan.setTermMonths(termMonths);
        return loanRepository.save(loan);
    }

    @Override
    public void deleteLoan(Long loanId, Long adminId) {
        requireAdmin(findUser(adminId, "Administrador no encontrado"));
        findLoan(loanId);
        loanRepository.deleteById(loanId);
    }

    @Override
    public Loan approveLoan(Long loanId, Long adminId) {
        Loan loan = findLoan(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessConflictException("Solo se pueden aprobar préstamos en estado PENDING");
        }

        User admin = findUser(adminId, "Administrador no encontrado");
        requireAdmin(admin);

        loan.setStatus(LoanStatus.APPROVED);
        loan.setReviewedBy(admin);
        return loanRepository.save(loan);
    }

    @Override
    public Loan rejectLoan(Long loanId, Long adminId) {
        Loan loan = findLoan(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessConflictException("Solo se pueden rechazar préstamos en estado PENDING");
        }

        User admin = findUser(adminId, "Administrador no encontrado");
        requireAdmin(admin);

        loan.setStatus(LoanStatus.REJECTED);
        loan.setReviewedBy(admin);
        return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Override
    public List<Loan> findAllPending() {
        return loanRepository.findByStatus(LoanStatus.PENDING);
    }

    @Override
    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    private void requireAdmin(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenOperationException("Solo un administrador puede gestionar préstamos");
        }
    }

    private Loan findLoan(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado: " + loanId));
    }

    private User findUser(Long userId, String message) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

}
