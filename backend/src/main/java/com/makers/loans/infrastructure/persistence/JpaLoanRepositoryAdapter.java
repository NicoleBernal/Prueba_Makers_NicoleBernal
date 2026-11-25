package com.makers.loans.infrastructure.persistence;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.application.port.out.LoanRepositoryPort;
import com.makers.loans.infrastructure.persistence.entity.LoanJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaLoanRepositoryAdapter implements LoanRepositoryPort {

    private final SpringDataLoanRepository repository;
    private final SpringDataUserRepository users;
    private final PersistenceMapper mapper;

    public JpaLoanRepositoryAdapter(SpringDataLoanRepository repository, SpringDataUserRepository users,
                                    PersistenceMapper mapper) {
        this.repository = repository;
        this.users = users;
        this.mapper = mapper;
    }

    @Override public Loan save(Loan loan) {
        LoanJpaEntity entity = mapper.toEntity(loan);
        entity.setUser(users.getReferenceById(loan.getUser().getId()));
        if (loan.getReviewedBy() != null) entity.setReviewedBy(users.getReferenceById(loan.getReviewedBy().getId()));
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
    @Override public Optional<Loan> findById(Long id) { return repository.findOne(id).map(mapper::toDomain); }
    @Override public List<Loan> findByUserId(Long userId) { return map(repository.findForUser(userId)); }
    @Override public List<Loan> findByStatus(LoanStatus status) { return map(repository.findWithStatus(status)); }
    @Override public List<Loan> findAll() { return map(repository.findEvery()); }
    private List<Loan> map(List<LoanJpaEntity> values) { return values.stream().map(mapper::toDomain).toList(); }
}
