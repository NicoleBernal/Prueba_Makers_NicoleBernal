package com.makers.loans.interfaces.rest.controller;

import com.makers.loans.interfaces.rest.adapter.LoanReactiveAdapter;
import com.makers.loans.interfaces.rest.dto.LoanRequest;
import com.makers.loans.interfaces.rest.dto.LoanResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api")
public class LoanController {

    private final LoanReactiveAdapter reactiveLoanService;

    public LoanController(LoanReactiveAdapter reactiveLoanService) {
        this.reactiveLoanService = reactiveLoanService;
    }

    @PostMapping("/loans")
    public Mono<ResponseEntity<LoanResponse>> createLoan(@Valid @RequestBody LoanRequest request,
                                                          Authentication auth) {
        return reactiveLoanService.create(auth.getName(),request.getAmount(), request.getTermMonths())
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @GetMapping("/loans/my-loans")
    public Flux<LoanResponse> getMyLoans(Authentication auth) {
        return reactiveLoanService.findLoansByCurrentUser(auth.getName());
    }

    @GetMapping("/loans/{id}")
    public Mono<LoanResponse> getLoan(@PathVariable Long id, Authentication auth) {
        return reactiveLoanService.findLoanById(id, auth.getName());
    }


    @GetMapping("/admin/loans")
    public Flux<LoanResponse> getAllLoans() {
        return reactiveLoanService.findAllLoans();
    }

    @GetMapping("/admin/loans/pending")
    public Flux<LoanResponse> getPendingLoans() {
        return reactiveLoanService.findPendingLoans();
    }

    @PutMapping("/admin/loans/{id}")
    public Mono<LoanResponse> updateLoan(@PathVariable Long id,
                                         @Valid @RequestBody LoanRequest request,
                                         Authentication auth) {
        return reactiveLoanService.update(id, auth.getName(), request.getAmount(), request.getTermMonths());
    }

    @DeleteMapping("/admin/loans/{id}")
    public Mono<ResponseEntity<Void>> deleteLoan(@PathVariable Long id, Authentication auth) {
        return reactiveLoanService.delete(id, auth.getName())
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/admin/loans/{id}/approve")
    public Mono<ResponseEntity<LoanResponse>> approveLoan(@PathVariable Long id, Authentication auth) {
        return reactiveLoanService.approve(id,auth.getName()).map(ResponseEntity::ok);
    }

    @PatchMapping("/admin/loans/{id}/reject")
    public Mono<ResponseEntity<LoanResponse>> rejectLoan(@PathVariable Long id, Authentication auth) {
        return reactiveLoanService.reject(id, auth.getName()).map(ResponseEntity::ok);
    }

}
