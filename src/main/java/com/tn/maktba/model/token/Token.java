package com.tn.maktba.model.token;

import com.tn.maktba.model.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "token", length = 2048)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType ;

    private boolean expired;

    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
