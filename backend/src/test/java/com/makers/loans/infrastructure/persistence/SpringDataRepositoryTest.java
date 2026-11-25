package com.makers.loans.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.domain.model.UserRole;
import com.makers.loans.infrastructure.persistence.entity.LoanJpaEntity;
import com.makers.loans.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class SpringDataRepositoryTest {

    @Autowired
    private SpringDataLoanRepository loans;

    @Autowired
    private SpringDataUserRepository users;

    @Autowired
    private EntityManager entityManager;

    private UserJpaEntity owner;
    private UserJpaEntity otherOwner;
    private UserJpaEntity reviewer;
    private LoanJpaEntity pending;
    private LoanJpaEntity approved;

    @BeforeEach
    void setUp() {
        owner = saveUser("owner@test.com", UserRole.USER);
        otherOwner = saveUser("other@test.com", UserRole.USER);
        reviewer = saveUser("admin@test.com", UserRole.ADMIN);
        pending = saveLoan(owner, LoanStatus.PENDING, null);
        approved = saveLoan(otherOwner, LoanStatus.APPROVED, reviewer);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findOneFetchesOwnerAndOptionalReviewer() {
        PersistenceUnitUtil persistence = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        LoanJpaEntity found = loans.findOne(approved.getId()).orElseThrow();

        assertThat(persistence.isLoaded(found.getUser())).isTrue();
        assertThat(persistence.isLoaded(found.getReviewedBy())).isTrue();
        assertThat(found.getUser().getEmail()).isEqualTo("other@test.com");
        assertThat(found.getReviewedBy().getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void findEveryUsesJoinFetchAndKeepsMostRecentLoansFirst() {
        PersistenceUnitUtil persistence = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        List<LoanJpaEntity> found = loans.findEvery();

        assertThat(found).extracting(LoanJpaEntity::getId)
                .containsExactly(approved.getId(), pending.getId());
        assertThat(found).allSatisfy(loan -> assertThat(persistence.isLoaded(loan.getUser())).isTrue());
        assertThat(found.get(0).getReviewedBy().getEmail()).isEqualTo("admin@test.com");
        assertThat(found.get(1).getReviewedBy()).isNull();
    }

    @Test
    void findForUserFiltersWithoutLeakingOtherUsersLoans() {
        List<LoanJpaEntity> found = loans.findForUser(owner.getId());

        assertThat(found).extracting(LoanJpaEntity::getId).containsExactly(pending.getId());
        assertThat(found).allMatch(loan -> loan.getUser().getId().equals(owner.getId()));
    }

    @Test
    void findWithStatusFiltersPendingLoans() {
        List<LoanJpaEntity> found = loans.findWithStatus(LoanStatus.PENDING);

        assertThat(found).extracting(LoanJpaEntity::getId).containsExactly(pending.getId());
        assertThat(found).extracting(LoanJpaEntity::getStatus).containsOnly(LoanStatus.PENDING);
    }

    @Test
    void userQueriesFindByIdAndEmail() {
        assertThat(users.findOne(owner.getId())).get().extracting(UserJpaEntity::getEmail)
                .isEqualTo("owner@test.com");
        assertThat(users.findOneByEmail("other@test.com")).get().extracting(UserJpaEntity::getId)
                .isEqualTo(otherOwner.getId());
    }

    private UserJpaEntity saveUser(String email, UserRole role) {
        UserJpaEntity user = new UserJpaEntity();
        user.setEmail(email);
        user.setPassword("hash");
        user.setName(email);
        user.setRole(role);
        return users.save(user);
    }

    private LoanJpaEntity saveLoan(UserJpaEntity user, LoanStatus status, UserJpaEntity reviewedBy) {
        LoanJpaEntity loan = new LoanJpaEntity();
        loan.setAmount(new BigDecimal("1200.00"));
        loan.setTermMonths(12);
        loan.setStatus(status);
        loan.setUser(user);
        loan.setReviewedBy(reviewedBy);
        return loans.save(loan);
    }
}
