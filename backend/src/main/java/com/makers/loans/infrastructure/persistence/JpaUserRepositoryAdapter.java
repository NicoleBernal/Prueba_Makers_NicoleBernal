package com.makers.loans.infrastructure.persistence;

import com.makers.loans.domain.model.User;
import com.makers.loans.application.port.out.UserRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final PersistenceMapper mapper;

    public JpaUserRepositoryAdapter(SpringDataUserRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override public User save(User user) { return mapper.toDomain(repository.save(mapper.toEntity(user))); }
    @Override public Optional<User> findById(Long id) { return repository.findOne(id).map(mapper::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return repository.findOneByEmail(email).map(mapper::toDomain); }
    @Override public List<User> findAll() { return repository.findAllOrdered().stream().map(mapper::toDomain).toList(); }
    @Override public void deleteById(Long id) { repository.deleteOne(id); }
    @Override public long count() { return repository.count(); }
}
