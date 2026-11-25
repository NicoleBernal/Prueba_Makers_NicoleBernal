package com.makers.loans.infrastructure.decorator;

import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.domain.model.Loan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

public class CachedLoanUseCase implements LoanUseCase {
    private final LoanUseCase delegate;

    public CachedLoanUseCase(LoanUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @CacheEvict(value = "userLoans", key = "#loan.user.id")
    public Loan createLoan(Loan loan) {
        return delegate.createLoan(loan);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "loans", key = "#loanId"),
            @CacheEvict(value = "userLoans", key = "#result.user.id")
    })
    public Loan updateLoan(Long loanId, Long adminId, BigDecimal amount, Integer termMonths) {
        return delegate.updateLoan(loanId, adminId, amount, termMonths);
    }

    @Override
    @CacheEvict(value = {"loans", "userLoans"}, allEntries = true)
    public void deleteLoan(Long loanId, Long adminId) {
        delegate.deleteLoan(loanId, adminId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "loans", key = "#loanId"),
            @CacheEvict(value = "userLoans", key = "#result.user.id")
    })
    public Loan approveLoan(Long loanId, Long adminId) {
        return delegate.approveLoan(loanId, adminId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "loans", key = "#loanId"),
            @CacheEvict(value = "userLoans", key = "#result.user.id")
    })
    public Loan rejectLoan(Long loanId, Long adminId) {
        return delegate.rejectLoan(loanId, adminId);
    }

    @Override
    @Cacheable(value = "loans", key = "#id")
    public Optional<Loan> findById(Long id) {
        return delegate.findById(id);
    }

    @Override
    @Cacheable(value = "userLoans", key = "#userId")
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
