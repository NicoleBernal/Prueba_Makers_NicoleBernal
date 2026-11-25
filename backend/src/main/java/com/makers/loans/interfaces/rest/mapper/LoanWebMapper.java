package com.makers.loans.interfaces.rest.mapper;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.interfaces.rest.dto.LoanResponse;
import org.springframework.stereotype.Component;

@Component
public class LoanWebMapper {
    public LoanResponse toResponse(Loan loan) {
        var borrower = loan.getUser();
        return new LoanResponse(
                loan.getId(),
                loan.getAmount(),
                loan.getTermMonths(),
                loan.getStatus().name(),
                borrower != null ? borrower.getEmail() : null,
                borrower != null ? borrower.getName() : null,
                loan.getCreatedAt(),
                loan.getUpdatedAt());
    }
}
