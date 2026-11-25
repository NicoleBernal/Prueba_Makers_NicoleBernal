package com.makers.loans.infrastructure.persistence.entity;

import com.makers.loans.domain.model.LoanStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="loans", indexes={@Index(name="idx_loans_user_id",columnList="user_id"),
        @Index(name="idx_loans_status",columnList="status")})
public class LoanJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;  // versión usada por el bloqueo optimista
    @Column(nullable=false,precision=15,scale=2) private BigDecimal amount;
    @Column(nullable=false) private Integer termMonths;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private LoanStatus status;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private UserJpaEntity user;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reviewed_by") private UserJpaEntity reviewedBy;
    @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist void prePersist(){if(createdAt==null)createdAt=LocalDateTime.now();if(status==null)status=LoanStatus.PENDING;}
    @PreUpdate void preUpdate(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;} public Long getVersion(){return version;} public void setVersion(Long v){version=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public Integer getTermMonths(){return termMonths;} public void setTermMonths(Integer v){termMonths=v;}
    public LoanStatus getStatus(){return status;} public void setStatus(LoanStatus v){status=v;} public UserJpaEntity getUser(){return user;} public void setUser(UserJpaEntity v){user=v;}
    public UserJpaEntity getReviewedBy(){return reviewedBy;} public void setReviewedBy(UserJpaEntity v){reviewedBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
