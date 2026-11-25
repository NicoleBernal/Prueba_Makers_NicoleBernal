package com.makers.loans.infrastructure.decorator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;

class TransactionalLoanUseCaseTest {

    private AnnotationConfigApplicationContext context;
    private LoanUseCase delegate;
    private LoanUseCase transactional;
    private PlatformTransactionManager transactions;
    private TransactionStatus status;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(TransactionTestConfig.class);
        delegate = context.getBean("delegate", LoanUseCase.class);
        transactional = context.getBean("transactional", LoanUseCase.class);
        transactions = context.getBean(PlatformTransactionManager.class);
        status = mock(TransactionStatus.class);
        when(transactions.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void successfulOperationCommitsTransaction() {
        Loan loan = Loan.builder().id(1L).status(LoanStatus.PENDING).build();
        when(delegate.createLoan(loan)).thenReturn(loan);

        transactional.createLoan(loan);

        verify(transactions).getTransaction(any(TransactionDefinition.class));
        verify(transactions).commit(status);
        verify(transactions, never()).rollback(status);
    }

    @Test
    void delegateFailureRollsBackTransactionAndPreservesError() {
        when(delegate.approveLoan(2L, 1L))
                .thenThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> transactional.approveLoan(2L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database failure");

        verify(transactions).rollback(status);
        verify(transactions, never()).commit(status);
    }

    @Test
    void updateRunsInsideATransaction() {
        Loan loan = Loan.builder().id(3L).status(LoanStatus.PENDING).build();
        when(delegate.updateLoan(3L, 1L, java.math.BigDecimal.TEN, 12)).thenReturn(loan);

        transactional.updateLoan(3L, 1L, java.math.BigDecimal.TEN, 12);

        verify(transactions).getTransaction(any(TransactionDefinition.class));
        verify(transactions).commit(status);
    }

    @Configuration
    @EnableTransactionManagement
    static class TransactionTestConfig {
        @Bean
        PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }

        @Bean("delegate")
        LoanUseCase delegate() {
            return mock(LoanUseCase.class);
        }

        @Bean("transactional")
        LoanUseCase transactional(@Qualifier("delegate") LoanUseCase delegate) {
            return new TransactionalLoanUseCase(delegate);
        }
    }
}
