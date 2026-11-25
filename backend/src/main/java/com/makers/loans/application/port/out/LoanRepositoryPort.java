package com.makers.loans.application.port.out;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {
    Loan save(Loan loan);
    void deleteById(Long id);
    Optional<Loan> findById(Long id);
    List<Loan> findByUserId(Long userId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findAll();
}
