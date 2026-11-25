package com.makers.loans.infrastructure.persistence.entity;

import com.makers.loans.domain.model.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=254) private String email;
    @Column(nullable=false) private String password;
    @Column(nullable=false, length=100) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private UserRole role;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt;

    @PrePersist void prePersist(){ if(createdAt==null) createdAt=LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public UserRole getRole(){return role;} public void setRole(UserRole v){role=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
