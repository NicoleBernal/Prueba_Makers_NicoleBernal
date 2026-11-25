package com.makers.loans.infrastructure.decorator;

import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.domain.model.Loan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class TransactionalLoanUseCase implements LoanUseCase {
    private final LoanUseCase delegate;

    public TransactionalLoanUseCase(LoanUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public Loan createLoan(Loan loan) {
        return delegate.createLoan(loan);
    }

    @Override
    public Loan updateLoan(Long loanId, Long adminId, BigDecimal amount, Integer termMonths) {
        return delegate.updateLoan(loanId, adminId, amount, termMonths);
    }

    @Override
    public void deleteLoan(Long loanId, Long adminId) {
        delegate.deleteLoan(loanId, adminId);
    }

    @Override
    public Loan approveLoan(Long loanId, Long adminId) {
        return delegate.approveLoan(loanId, adminId);
    }

    @Override
    public Loan rejectLoan(Long loanId, Long adminId) {
        return delegate.rejectLoan(loanId, adminId);
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return delegate.findById(id);
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        return delegate.findByUserId(userId);
    }

    @Override
    public List<Loan> findAllPending() {
        return delegate.findAllPending();
    }

    @Override
    public List<Loan> findAll() {
        return delegate.findAll();
    }
}
