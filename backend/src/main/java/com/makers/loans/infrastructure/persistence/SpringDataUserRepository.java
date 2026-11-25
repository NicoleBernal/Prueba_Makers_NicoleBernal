package com.makers.loans.infrastructure.persistence;

import com.makers.loans.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    @Query("select u from UserJpaEntity u where u.id = :id")
    Optional<UserJpaEntity> findOne(@Param("id") Long id);
    @Query("select u from UserJpaEntity u where u.email = :email")
    Optional<UserJpaEntity> findOneByEmail(@Param("email") String email);
    @Query("select u from UserJpaEntity u order by u.createdAt desc")
    List<UserJpaEntity> findAllOrdered();
    @Modifying
    @Query("delete from UserJpaEntity u where u.id = :id")
    void deleteOne(@Param("id") Long id);
}
