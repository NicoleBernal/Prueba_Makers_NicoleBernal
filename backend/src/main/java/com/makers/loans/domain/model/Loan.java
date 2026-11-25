package com.makers.loans.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Loan {
    private Long id;
    private Long version;
    private BigDecimal amount;
    private Integer termMonths;
    private LoanStatus status;
    private User user;
    private User reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Loan() {}

    public Loan(Long id, Long version, BigDecimal amount, Integer termMonths, LoanStatus status,
                User user, User reviewedBy,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.version = version; this.amount = amount; this.termMonths = termMonths;
        this.status = status == null ? LoanStatus.PENDING : status; this.user = user;
        this.reviewedBy = reviewedBy;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }
    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer value) { termMonths = value; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus value) { status = value; }
    public User getUser() { return user; }
    public void setUser(User value) { user = value; }
    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User value) { reviewedBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }

    public static final class Builder {
        private Long id, version; private BigDecimal amount; private Integer termMonths;
        private LoanStatus status; private User user, reviewedBy;
        private LocalDateTime createdAt, updatedAt;
        public Builder id(Long v){id=v;return this;} public Builder version(Long v){version=v;return this;}
        public Builder amount(BigDecimal v){amount=v;return this;} public Builder termMonths(Integer v){termMonths=v;return this;}
        public Builder status(LoanStatus v){status=v;return this;} public Builder user(User v){user=v;return this;}
        public Builder reviewedBy(User v){reviewedBy=v;return this;}
        public Builder createdAt(LocalDateTime v){createdAt=v;return this;} public Builder updatedAt(LocalDateTime v){updatedAt=v;return this;}
        public Loan build(){return new Loan(id,version,amount,termMonths,status,user,reviewedBy,createdAt,updatedAt);}
    }
}
