package com.makers.loans.application.port.in;

import com.makers.loans.domain.model.Loan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanUseCase {
    Loan createLoan(Loan loan);
    Loan updateLoan(Long loanId, Long adminId, BigDecimal amount, Integer termMonths);
    void deleteLoan(Long loanId, Long adminId);
    Loan approveLoan(Long loanId, Long adminId);
    Loan rejectLoan(Long loanId, Long adminId);
    Optional<Loan> findById(Long id);
    List<Loan> findByUserId(Long userId);
    List<Loan> findAllPending();
    List<Loan> findAll();
}
