package com.makers.loans.infrastructure.persistence;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.User;
import com.makers.loans.infrastructure.persistence.entity.LoanJpaEntity;
import com.makers.loans.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
class PersistenceMapper {
    User toDomain(UserJpaEntity e) {
        if(e==null)return null;
        return new User(e.getId(),e.getEmail(),e.getPassword(),e.getName(),e.getRole(),e.getCreatedAt());
    }
    UserJpaEntity toEntity(User d) {
        if(d==null)return null; UserJpaEntity e=new UserJpaEntity();
        e.setId(d.getId());e.setEmail(d.getEmail());e.setPassword(d.getPassword());e.setName(d.getName());
        e.setRole(d.getRole());e.setCreatedAt(d.getCreatedAt());return e;
    }
    Loan toDomain(LoanJpaEntity e) {
        if(e==null)return null;
        return new Loan(e.getId(),e.getVersion(),e.getAmount(),e.getTermMonths(),e.getStatus(),
                toDomain(e.getUser()),toDomain(e.getReviewedBy()),e.getCreatedAt(),e.getUpdatedAt());
    }
    LoanJpaEntity toEntity(Loan d) {
        LoanJpaEntity e=new LoanJpaEntity();e.setId(d.getId());e.setVersion(d.getVersion());e.setAmount(d.getAmount());
        e.setTermMonths(d.getTermMonths());e.setStatus(d.getStatus());e.setUser(toEntity(d.getUser()));
        e.setReviewedBy(toEntity(d.getReviewedBy()));
        e.setCreatedAt(d.getCreatedAt());e.setUpdatedAt(d.getUpdatedAt());return e;
    }
}
