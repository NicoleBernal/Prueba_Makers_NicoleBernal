package com.makers.loans.infrastructure.persistence;

import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.infrastructure.persistence.entity.LoanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SpringDataLoanRepository extends JpaRepository<LoanJpaEntity, Long> {
    @Query("select l from LoanJpaEntity l join fetch l.user left join fetch l.reviewedBy where l.id=:id")
    Optional<LoanJpaEntity> findOne(@Param("id") Long id);
    @Query("select l from LoanJpaEntity l join fetch l.user left join fetch l.reviewedBy order by l.createdAt desc, l.id desc")
    List<LoanJpaEntity> findEvery();
    @Query("select l from LoanJpaEntity l join fetch l.user left join fetch l.reviewedBy where l.user.id=:userId order by l.createdAt desc, l.id desc")
    List<LoanJpaEntity> findForUser(@Param("userId") Long userId);
    @Query("select l from LoanJpaEntity l join fetch l.user left join fetch l.reviewedBy where l.status=:status order by l.createdAt desc, l.id desc")
    List<LoanJpaEntity> findWithStatus(@Param("status") LoanStatus status);
}
