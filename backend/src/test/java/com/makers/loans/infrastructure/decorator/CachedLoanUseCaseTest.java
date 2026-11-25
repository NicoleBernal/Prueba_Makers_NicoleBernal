package com.makers.loans.infrastructure.decorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.domain.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class CachedLoanUseCaseTest {

    private AnnotationConfigApplicationContext context;
    private LoanUseCase delegate;
    private LoanUseCase cached;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(CacheTestConfig.class);
        delegate = context.getBean("delegate", LoanUseCase.class);
        cached = context.getBean("cached", LoanUseCase.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void findByIdUsesCacheAfterFirstInvocation() {
        Loan loan = loan(8L);
        when(delegate.findById(8L)).thenReturn(Optional.of(loan));

        assertThat(cached.findById(8L)).containsSame(loan);
        assertThat(cached.findById(8L)).containsSame(loan);

        verify(delegate, times(1)).findById(8L);
    }

    @Test
    void approveEvictsCachedLoanSoNextReadReloadsIt() {
        Loan original = loan(9L);
        Loan updated = loan(9L);
        updated.setStatus(LoanStatus.APPROVED);
        when(delegate.findById(9L)).thenReturn(Optional.of(original), Optional.of(updated));
        when(delegate.approveLoan(9L, 1L)).thenReturn(updated);

        cached.findById(9L);
        cached.approveLoan(9L, 1L);

        assertThat(cached.findById(9L)).get()
                .extracting(Loan::getStatus)
                .isEqualTo(LoanStatus.APPROVED);
        verify(delegate, times(2)).findById(9L);
    }

    @Test
    void updateEvictsLoanAndOwnerListCaches() {
        Loan original = loan(15L);
        Loan edited = loan(15L);
        edited.setAmount(new BigDecimal("7500.00"));
        when(delegate.findById(15L)).thenReturn(Optional.of(original), Optional.of(edited));
        when(delegate.findByUserId(4L)).thenReturn(List.of(original), List.of(edited));
        when(delegate.updateLoan(15L, 1L, new BigDecimal("7500.00"), 12)).thenReturn(edited);

        cached.findById(15L);
        cached.findByUserId(4L);
        cached.updateLoan(15L, 1L, new BigDecimal("7500.00"), 12);

        assertThat(cached.findById(15L)).get().extracting(Loan::getAmount).isEqualTo(new BigDecimal("7500.00"));
        assertThat(cached.findByUserId(4L)).extracting(Loan::getAmount).containsExactly(new BigDecimal("7500.00"));
        verify(delegate, times(2)).findById(15L);
        verify(delegate, times(2)).findByUserId(4L);
    }

    private Loan loan(Long id) {
        return Loan.builder()
                .id(id)
                .amount(new BigDecimal("1000.00"))
                .termMonths(12)
                .status(LoanStatus.PENDING)
                .user(User.builder().id(4L).email("owner@test.com").build())
                .build();
    }

    @Configuration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("loans", "userLoans");
        }

        @Bean("delegate")
        LoanUseCase delegate() {
            return mock(LoanUseCase.class);
        }

        @Bean("cached")
        LoanUseCase cached(LoanUseCase delegate) {
            return new CachedLoanUseCase(delegate);
        }
    }
}
