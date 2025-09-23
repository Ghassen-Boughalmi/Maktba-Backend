package com.tn.maktba.model.token;

import com.tn.maktba.model.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String refreshToken;
    private boolean expired;
    private boolean revoked;
    private Date issuedAt;
    private Date expiresAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}