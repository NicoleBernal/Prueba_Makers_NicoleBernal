package com.makers.loans.infrastructure.config;

import com.makers.loans.application.port.in.AuthUseCase;
import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.application.port.in.UserUseCase;
import com.makers.loans.application.port.out.LoanRepositoryPort;
import com.makers.loans.application.port.out.PasswordPort;
import com.makers.loans.application.port.out.TokenPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.application.service.AuthService;
import com.makers.loans.application.service.LoanServiceImpl;
import com.makers.loans.application.service.UserServiceImpl;
import com.makers.loans.infrastructure.decorator.CachedLoanUseCase;
import com.makers.loans.infrastructure.decorator.TransactionalLoanUseCase;
import com.makers.loans.infrastructure.decorator.TransactionalUserUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ApplicationConfig {
    @Bean("transactionalLoanUseCase")
    public LoanUseCase transactionalLoanUseCase(
            LoanRepositoryPort loans,
            UserRepositoryPort users) {
        LoanUseCase core = new LoanServiceImpl(loans, users);
        return new TransactionalLoanUseCase(core);
    }

    @Bean
    @Primary
    public LoanUseCase loanUseCase(
            @Qualifier("transactionalLoanUseCase") LoanUseCase transactional) {
        return new CachedLoanUseCase(transactional);
    }

    @Bean
    @Primary
    public UserUseCase userUseCase(
            UserRepositoryPort users,
            PasswordPort passwords) {
        return new TransactionalUserUseCase(new UserServiceImpl(users, passwords));
    }

    @Bean
    public AuthUseCase authUseCase(
            UserRepositoryPort users,
            PasswordPort passwords,
            TokenPort tokens) {
        return new AuthService(users, passwords, tokens);
    }
}
