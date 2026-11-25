package com.makers.loans.infrastructure.config;

import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.LoanStatus;
import com.makers.loans.domain.model.User;
import com.makers.loans.domain.model.UserRole;
import com.makers.loans.application.port.out.LoanRepositoryPort;
import com.makers.loans.application.port.out.UserRepositoryPort;
import com.makers.loans.application.port.out.PasswordPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Profile("!prod")
public class DataInitializer implements ApplicationRunner {

    private final UserRepositoryPort userRepository;
    private final LoanRepositoryPort loanRepository;
    private final PasswordPort passwordEncoder;
    private final String demoPassword;

    public DataInitializer(UserRepositoryPort userRepository,
                           LoanRepositoryPort loanRepository,
                           PasswordPort passwordEncoder,
                           @Value("${DEMO_PASSWORD:123}") String demoPassword) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() != 0) {
            return;
        }

        User admin = userRepository.save(User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode(demoPassword))
                .name("Administrador")
                .role(UserRole.ADMIN)
                .build());
        User user = userRepository.save(User.builder()
                .email("usuario@test.com")
                .password(passwordEncoder.encode(demoPassword))
                .name("Usuario Demo")
                .role(UserRole.USER)
                .build());

        loanRepository.save(Loan.builder()
                .amount(new BigDecimal("5000.00"))
                .termMonths(12)
                .status(LoanStatus.PENDING)
                .user(user)
                .build());
        loanRepository.save(Loan.builder()
                .amount(new BigDecimal("10000.00"))
                .termMonths(24)
                .status(LoanStatus.APPROVED)
                .user(user)
                .reviewedBy(admin)
                .build());
        loanRepository.save(Loan.builder()
                .amount(new BigDecimal("2500.00"))
                .termMonths(6)
                .status(LoanStatus.REJECTED)
                .user(user)
                .reviewedBy(admin)
                .build());
    }
}
