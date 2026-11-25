package com.makers.loans.interfaces.rest.adapter;

import com.makers.loans.application.exception.ForbiddenOperationException;
import com.makers.loans.application.exception.ResourceNotFoundException;
import com.makers.loans.application.port.in.LoanUseCase;
import com.makers.loans.application.port.in.UserUseCase;
import com.makers.loans.domain.model.Loan;
import com.makers.loans.domain.model.User;
import com.makers.loans.interfaces.rest.dto.LoanResponse;
import com.makers.loans.interfaces.rest.mapper.LoanWebMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class LoanReactiveAdapter {
    private final LoanUseCase loans;
    private final UserUseCase users;
    private final LoanWebMapper loanMapper;

    public LoanReactiveAdapter(LoanUseCase loans, UserUseCase users, LoanWebMapper loanMapper) {
        this.loans = loans;
        this.users = users;
        this.loanMapper = loanMapper;
    }

    public Flux<LoanResponse> findLoansByCurrentUser(String email) {
        return Flux.defer(() -> {
                    User user = users.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
                    return Flux.fromIterable(loans.findByUserId(user.getId()));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(loanMapper::toResponse);
    }

    public Mono<LoanResponse> findLoanById(Long loanId, String email) {
        return Mono.fromCallable(() -> {
                    Loan loan = loans.findById(loanId)
                            .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));
                    if (!loan.getUser().getEmail().equalsIgnoreCase(email)) {
                        throw new ForbiddenOperationException("No puedes consultar préstamos de otros usuarios");
                    }
                    return loanMapper.toResponse(loan);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<LoanResponse> findAllLoans() {
        return findMany(loans::findAll);
    }

    public Flux<LoanResponse> findPendingLoans() {
        return findMany(loans::findAllPending);
    }

    private Flux<LoanResponse> findMany(Supplier<List<Loan>> query) {
        return Flux.defer(() -> Flux.fromIterable(query.get()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(loanMapper::toResponse);
    }

    public Mono<LoanResponse> create(String email, BigDecimal amount, Integer termMonths) {
        return Mono.fromCallable(() -> {
            User user = users.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            Loan loan = Loan.builder().amount(amount).termMonths(termMonths).user(user).build();
            return loanMapper.toResponse(loans.createLoan(loan));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<LoanResponse> update(Long id, String email, BigDecimal amount, Integer termMonths) {
        return reviewUser(email)
                .map(admin -> loanMapper.toResponse(loans.updateLoan(id, admin.getId(), amount, termMonths)));
    }

    public Mono<Void> delete(Long id, String email) {
        return reviewUser(email)
                .doOnNext(admin -> loans.deleteLoan(id, admin.getId()))
                .then();
    }

    public Mono<LoanResponse> approve(Long id, String email) {
        return reviewUser(email)
                .map(user -> loanMapper.toResponse(loans.approveLoan(id, user.getId())));
    }

    public Mono<LoanResponse> reject(Long id, String email) {
        return reviewUser(email)
                .map(user -> loanMapper.toResponse(loans.rejectLoan(id, user.getId())));
    }

    private Mono<User> reviewUser(String email) {
        return Mono.fromCallable(() -> users.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado")))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
